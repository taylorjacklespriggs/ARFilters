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

import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;
import com.taylorjs.hproject.arfilters.R;

import static com.arfilters.GLTools.FrameBuffer;

class DarknessFilter extends BufferedFilter {

    static DarknessFilter create(ShaderGenerator camGen,
                                 ShaderGenerator texGen,
                                 FrameBuffer front,
                                 FrameBuffer back,
                                 FrameBuffer camera,
                                 Matrix3x3Data vertMatData,
                                 float halfLife,
                                 float sensitivity,
                                 VertexMatrixUpdater ptVmi) {
        camGen.setComputeColor(R.raw.highq_gray);
        Shader ctt = camGen.generateShader();
        texGen.setComputeColor(R.raw.pixel_stats);
        Shader stat = texGen.generateShader();
        texGen.setComputeColor(R.raw.darkness_passthrough);
        Shader pt = texGen.generateShader();
        return new DarknessFilter(ctt, stat, pt, front, back, camera, vertMatData, halfLife, sensitivity, ptVmi);
    }

    @Override
    protected void renderToBuffers() {

        // render camera to texture
        cameraBuffer.enable();
        cameraToTextureShader.draw();

        // do stats for each pixel
        bufferTextureData.newTextureLocation(backBuffer.getTextureID());

        frontBuffer.enable();
        statsShader.draw();

        // update bufferTextureID for passThrough
        bufferTextureData.newTextureLocation(frontBuffer.getTextureID());

        // swap stat frameBuffers
        FrameBuffer tmp = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = tmp;

    }

    private DarknessFilter(Shader ctt, Shader stat, Shader pt,
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
        float rawScale = 1f-fadeAmount;
        FloatData scale = new FloatData(rawScale);

        statsShader.addUniform("u_FadeAmount", new FloatData(fadeAmount));

        pt.addUniform("u_Scale", scale);
        pt.addUniform("u_VarScale", new FloatData(rawScale/sensitivity));
    }

    private final Shader cameraToTextureShader, statsShader;
    private FrameBuffer frontBuffer, backBuffer;
    private final FrameBuffer cameraBuffer;
    private final TextureLocationData
            bufferTextureData;

}
