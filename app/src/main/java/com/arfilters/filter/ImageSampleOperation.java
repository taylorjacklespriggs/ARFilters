/*
 * Copyright (C) 2017  Taylor Jackle Spriggs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.arfilters.filter;

import android.opengl.GLES20;
import android.util.Log;

import static com.arfilters.GLTools.FrameBuffer;
import com.arfilters.shader.Shader;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class allows operations to sample a buffer
 */
public abstract class ImageSampleOperation extends RTTOperation {

    private static final String TAG = ImageSampleOperation.class.getName();

    @Override
    public void cleanup() {
        processingThread.interrupt();
    }

    protected interface ImageSampler {
        void feed(int x, int y, int r, int g, int b, int a);
        void finish();
    }

    protected abstract ImageSampler createImageSampler();

    private int getPixel(int x, int y, int channel) {
        int pixel = imageData.get((y*sampleBuffer.getWidth()+x)*4+channel);
        if(pixel < 0)
            pixel += 256;
        return pixel;
    }

    private void sampleFrameBuffer() {
        synchronized(this) {
            if(requiresUpdate) {
                if (imageSampler != null) {
                    imageSampler.finish();
                    imageSampler = null;
                }
                if (nFrames >= updateFrequency) {
                    sampleBuffer.enable();
                    setVertexMatrix(sampleMatrix);
                    sampleShader.draw();
                    setIdentityVertexMatrix();
                    imageLock.lock();
                    GLES20.glReadPixels(0, 0, sampleBuffer.getWidth(), sampleBuffer.getHeight(),
                            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, imageData);
                    nFrames = 0;
                    requiresUpdate = false;
                    imageAvailable.signal();
                    imageLock.unlock();
                }
            }
            ++nFrames;
        }
    }

    protected void preSample() {
    }

    protected void postSample() {
    }

    @Override
    protected final void renderToBuffers() {
        super.renderToBuffers();
        preSample();
        sampleFrameBuffer();
        postSample();
    }

    ImageSampleOperation(Shader rtt, Shader pt, Shader sampShader, FrameBuffer fb,
                         FrameBuffer sampBuffer,
                         Matrix3x3Data vertMatData,
                         VertexMatrixUpdater ptVmi,
                         float windowScale,
                         int updateFreq, String name) {
        super(rtt, pt, fb, vertMatData, ptVmi, name);
        sampleMatrix = new float[] {
                1f/windowScale, 0, 0,
                0, 1f/windowScale, 0,
                0, 0, 1f
        };
        sampleBuffer = sampBuffer;
        sampleShader = sampShader;
        sampleShader.addUniform("u_Texture", new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 0, fb.getTextureID()));
        updateFrequency = updateFreq;
        imageLock = new ReentrantLock(true);
        imageAvailable = imageLock.newCondition();
        requiresUpdate = true;
        imageData = ByteBuffer.allocateDirect(sampleBuffer.getWidth()*sampleBuffer.getHeight()*4);
        processingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                imageLock.lock();
                try {
                    while (true) {
                        imageAvailable.await();
                        imageSampler = createImageSampler();
                        for (int x = 0; x < sampleBuffer.getWidth(); ++x)
                            for (int y = 0; y < sampleBuffer.getHeight(); ++y)
                                imageSampler.feed(x, y, getPixel(x, y, 0),
                                        getPixel(x, y, 1), getPixel(x, y, 2),
                                        getPixel(x, y, 3));
                        synchronized (this) {
                            requiresUpdate = true;
                        }
                    }
                } catch(InterruptedException e) {
                    Log.i(TAG, "Processing thread interrupted");
                } finally {
                    imageLock.unlock();
                }
            }
        });
        processingThread.start();
    }

    private final float[] sampleMatrix;
    private Shader sampleShader;
    private final FrameBuffer sampleBuffer;
    private final Lock imageLock;
    private final Condition imageAvailable;
    private boolean requiresUpdate;
    private final ByteBuffer imageData;
    private final Thread processingThread;
    private final int updateFrequency;
    private int nFrames;
    private ImageSampler imageSampler;
}
