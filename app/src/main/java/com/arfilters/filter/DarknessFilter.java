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
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;

import static com.arfilters.GLTools.FrameBuffer;

public class DarknessFilter extends SingleShaderFilter {

    private static final String TAG = "SingleShaderFilter";

    private static final float[] IDENTITY = new float[] {
            1f,0f,0f,0f,1f,0f,0f,0f,1f
    };

    @Override
    public void prepareView() {

        vertexMatrixData.updateData(IDENTITY);

        // render camera to texture
        cameraBuffer.enable();
        // now cameraBuffer has original framebufferID
        cameraToTextureShader.draw();

        GLTools.checkGLError(TAG, "draw camera passThrough");

        // do stats for each pixel
        bufferTextureData.newTextureLocation(backBuffer.getTextureID());

        frontBuffer.enable();
        statsShader.draw();

        GLTools.checkGLError(TAG, "draw stats shader");

        // update bufferTextureID for passThrough
        bufferTextureData.newTextureLocation(frontBuffer.getTextureID());

        // swap stat frameBuffers
        FrameBuffer tmp = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = tmp;

        // reset original framebufferID
        cameraBuffer.disable();

        GLTools.checkGLError(TAG, "reset frameBuffer");

    }

    public DarknessFilter(Shader ctt, Shader stat, Shader pt,
                          FrameBuffer front,
                          FrameBuffer back,
                          FrameBuffer camera,
                          Matrix3x3Data vertMatData,
                          float halfLife,
                          float sensitivity,
                          VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi);
        cameraToTextureShader = ctt;
        statsShader = stat;
        frontBuffer = front;
        backBuffer = back;
        cameraBuffer = camera;

        bufferTextureData = new TextureLocationData(GLES20.GL_TEXTURE_2D, 1, backBuffer.getTextureID());

        TextureLocationData camLoc = new TextureLocationData(GLES20.GL_TEXTURE_2D, 0, cameraBuffer.getTextureID());

        pt.addUniform("u_Texture", camLoc);
        pt.addUniform("u_BufferTexture", bufferTextureData);

        statsShader.addUniform("u_Texture", camLoc);
        statsShader.addUniform("u_BufferTexture", bufferTextureData);

        final float fps = 60f;
        float frames = fps*halfLife; // num frames
        float fadeAmount = (float)Math.pow(.5, 1./frames); // fading factor
        FloatData scale = new FloatData(1f-fadeAmount);

        statsShader.addUniform("u_FadeAmount", new FloatData(fadeAmount));
        statsShader.addUniform("u_Scale", scale);
        pt.addUniform("u_Scale", scale);
        pt.addUniform("u_Sensitivity", new FloatData(sensitivity));
    }

    private final Shader cameraToTextureShader, statsShader;
    private FrameBuffer frontBuffer, backBuffer;
    private final FrameBuffer cameraBuffer;
    private final TextureLocationData
            bufferTextureData;

}
