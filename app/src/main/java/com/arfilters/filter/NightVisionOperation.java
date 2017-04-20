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

import com.arfilters.GLTools;
import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;
import com.taylorjs.hproject.arfilters.R;

import java.nio.ByteBuffer;

import static com.arfilters.GLTools.FrameBuffer;

class NightVisionOperation extends ImageSampleOperation {

    private static final String TAG = NightVisionOperation.class.getName();

    private static final int HIST_MAJOR_COUNT = 256;
    private static final int HIST_MINOR_COUNT = 16;

    static NightVisionOperation create(ShaderGenerator camGen,
                                       ShaderGenerator texGen,
                                       int halfLife,
                                       FrameBuffer front,
                                       FrameBuffer back,
                                       FrameBuffer camera,
                                       FrameBuffer sampleBuffer,
                                       Matrix3x3Data vertMatData,
                                       VertexMatrixUpdater ptVmi) {
        float r = (float)Math.pow(.5, 1./halfLife); // fading factor

        // generate camera to texture shader
        Shader ctt = camGen.generateShader();

        // generate sampling passthrough shader
        texGen.setComputeColor(R.raw.passthrough);
        Shader sampShader = texGen.generateShader();

        // generate mixing shader
        texGen.setComputeColor(R.raw.monochrome_mixing_texture);
        Shader mix = texGen.generateShader();

        // create the passThrough shader
        texGen.setComputeColor(R.raw.monochrome_finalpass);
        Shader pt = texGen.generateShader();

        return new NightVisionOperation(ctt, mix, pt, sampShader, r, 1f, 0, front, back,
                camera, sampleBuffer, vertMatData, ptVmi);
    }

    private class ContrastSampler implements ImageSampler {
        @Override
        public void feed(int x, int y, int r, int g, int b, int a) {
            int val = (r*256+g)*HIST_MINOR_COUNT/256;
            ++histogram[val];
        }
        @Override
        public void finish() {
            // clear out the lowest value and add up the bins
            histogram[0] = 0;
            for(int i = 0; i < histogram.length-1; ++i)
                histogram[i+1] += histogram[i];

            // put the normalized value in the byte buffer
            cdf.position(0);
            for(long v: histogram)
                cdf.put((byte) (v * 255 / histogram[histogram.length-1]));

            // update the histogram texture
            cdf.position(0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureLocation);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, histogram.length, 1,
                    GLES20.GL_ALPHA, GLES20.GL_UNSIGNED_BYTE, cdf);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        backBuffer.clear(0f, 0f, 0f, 0f);
    }

    @Override
    protected ImageSampler createImageSampler() {
        for(int i = 0; i < histogram.length; ++i)
            histogram[i] = 0;
        return new ContrastSampler();
    }

    @Override
    protected void preSample() {

        // mix cameraBuffer and backBuffer
        backTextureData.newTextureLocation(backBuffer.getTextureID());
        frontBuffer.enable();
        mixingShader.draw();

        // update texture location for sample shader
        frontTextureData.newTextureLocation(frontBuffer.getTextureID());

    }

    @Override
    public void postSample() {

        // update frontTextureID for final pass
        frontTextureData.newTextureLocation(frontBuffer.getTextureID());

        // swap framebuffers
        FrameBuffer tmp = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = tmp;

    }

    private NightVisionOperation(Shader ctt, Shader mix, Shader pt, Shader sampShader,
                                 float r, float windowScale, int updateFreq,
                                 FrameBuffer front, FrameBuffer back,
                                 FrameBuffer camera, FrameBuffer sampleBuffer,
                                 Matrix3x3Data vertMatData, VertexMatrixUpdater ptVmi) {
        super(ctt, pt, sampShader, camera, sampleBuffer, vertMatData, ptVmi,
                windowScale, updateFreq, "Night Vision");

        histogram = new long[HIST_MINOR_COUNT*HIST_MAJOR_COUNT];
        cdf = ByteBuffer.allocateDirect(histogram.length);
        for(int i = 0; i < HIST_MAJOR_COUNT; ++i)
            for(int j = 0; j < HIST_MINOR_COUNT; ++j)
                cdf.put((byte) i);
        cdf.position(0);
        mixingShader = mix;
        frontBuffer = front;
        backBuffer = back;
        frontTextureData = new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 0, front.getTextureID());
        backTextureData = new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 1, back.getTextureID());

        sampShader.addUniform("u_Texture", frontTextureData);

        TextureLocationData camLoc = new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 0, camera.getTextureID());
        FloatData secondAmount = new FloatData(r);
        mixingShader.addUniform("u_FadeAmount", secondAmount);
        mixingShader.addUniform("u_Texture", camLoc);
        mixingShader.addUniform("u_AlternateTexture", backTextureData);

        // create texture for storing histogram data
        textureLocation = GLTools.genTexture(GLES20.GL_TEXTURE_2D,
                GLES20.GL_NEAREST, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_ALPHA, histogram.length, 1, 0,
                GLES20.GL_ALPHA, GLES20.GL_UNSIGNED_BYTE, cdf);
        GLTools.checkGLError(TAG, "generate image");

        pt.addUniform("u_CDF", new TextureLocationData(GLES20.GL_TEXTURE_2D, 1,
                textureLocation));

        pt.addUniform("u_Texture", frontTextureData);
    }

    private final int textureLocation;
    private final long[] histogram;
    private final ByteBuffer cdf;
    private final Shader mixingShader;
    private FrameBuffer frontBuffer, backBuffer;
    private final TextureLocationData
            frontTextureData,
            backTextureData;

}
