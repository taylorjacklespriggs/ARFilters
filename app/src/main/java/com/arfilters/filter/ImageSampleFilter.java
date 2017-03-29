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

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ImageSampleFilter extends RTTFilter {

    private static final String TAG = ImageSampleFilter.class.getName();

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
        int pixel = imageData.get((y*windowW+x)*4+channel);
        if(pixel < 0)
            pixel += 256;
        return pixel;
    }

    @Override
    protected void renderToBuffers() {
        super.renderToBuffers();
        FrameBuffer fb = getFrameBuffer();
        synchronized(this) {
            if(requiresUpdate) {
                if (imageSampler != null) {
                    imageSampler.finish();
                    imageSampler = null;
                }
                if (nFrames >= updateFrequency) {
                    imageLock.lock();
                    GLES20.glReadPixels(windowX, windowY, windowW, windowH,
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

    public ImageSampleFilter(Shader rtt, Shader pt, FrameBuffer fb,
                             Matrix3x3Data vertMatData,
                             VertexMatrixUpdater ptVmi,
                             int x, int y, int w, int h,
                             int updateFreq, String name) {
        super(rtt, pt, fb, vertMatData, ptVmi, name);
        windowX = x;
        windowY = y;
        windowW = w;
        windowH = h;
        updateFrequency = updateFreq;
        imageLock = new ReentrantLock(true);
        imageAvailable = imageLock.newCondition();
        requiresUpdate = true;
        imageData = ByteBuffer.allocateDirect(windowW*windowH*4);
        forceStop = false;
        processingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                imageLock.lock();
                try {
                    while (true) {
                        imageAvailable.await();
                        imageSampler = createImageSampler();
                        for(int i = 0; i < windowW; ++i)
                            for(int j = 0; j < windowH; ++j)
                                imageSampler.feed(i, j, getPixel(i, j, 0),
                                        getPixel(i, j, 1), getPixel(i, j, 2),
                                        getPixel(i, j, 3));
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

    private final Lock imageLock;
    private final Condition imageAvailable;
    private boolean requiresUpdate;
    private final ByteBuffer imageData;
    private final Thread processingThread;
    private final int windowX, windowY, windowW, windowH;
    private final int updateFrequency;
    private int nFrames;
    private ImageSampler imageSampler;
    private boolean forceStop;
}
