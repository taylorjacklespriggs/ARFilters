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
import com.arfilters.shader.data.Vector2Data;
import com.taylorjs.hproject.arfilters.R;

import static com.arfilters.GLTools.FrameBuffer;

class LocalContrastFilter extends BufferedFilter {

    static LocalContrastFilter create(ShaderGenerator camGen,
                                      ShaderGenerator texGen,
                                      FrameBuffer front,
                                      FrameBuffer back,
                                      FrameBuffer camera,
                                      Matrix3x3Data vertMatData,
                                      VertexMatrixUpdater ptVmi) {
        camGen.setComputeColor(R.raw.passthrough);
        Shader ctt = camGen.generateShader();
        texGen.setComputeColor(R.raw.min_max_first);
        Shader minMaxFirst = texGen.generateShader();
        texGen.setComputeColor(R.raw.min_max_second);
        Shader minMaxSecond = texGen.generateShader();
        texGen.setComputeColor(R.raw.local_contrast_fp);
        Shader pt = texGen.generateShader();
        return new LocalContrastFilter(ctt, minMaxFirst, minMaxSecond, pt, front, back, camera, vertMatData, ptVmi);
    }

    @Override
    public void initialize() {
        frontBuffer.enable();
        GLES20.glClearColor(0f, 1f, 0f, 0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    protected void renderToBuffers() {

        // render camera to texture
        cameraBuffer.enable();
        cameraToTextureShader.draw();

        // do stats for each pixel
        frontBuffer.enable();
        bufferTextureData.newTextureLocation(backBuffer.getTextureID());
        deltaData.updateData(new float[] {1f/frontBuffer.getWidth(), 0f});
        minMaxFirstShader.draw();

        backBuffer.enable();
        bufferTextureData.newTextureLocation(frontBuffer.getTextureID());
        deltaData.updateData(new float[] {0f, 1f/frontBuffer.getHeight()});
        minMaxSecondShader.draw();

        // update bufferTextureID for passThrough
        bufferTextureData.newTextureLocation(backBuffer.getTextureID());

    }

    private LocalContrastFilter(Shader ctt, Shader minMaxFirst, Shader minMaxSecond, Shader pt,
                                FrameBuffer front,
                                FrameBuffer back,
                                FrameBuffer camera,
                                Matrix3x3Data vertMatData,
                                VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi, "Darkness");
        cameraToTextureShader = ctt;
        minMaxFirstShader = minMaxFirst;
        minMaxSecondShader = minMaxSecond;
        frontBuffer = front;
        backBuffer = back;
        cameraBuffer = camera;

        deltaData = new Vector2Data();

        bufferTextureData = new TextureLocationData(GLES20.GL_TEXTURE_2D, 1, backBuffer.getTextureID());

        TextureLocationData camLoc = new TextureLocationData(GLES20.GL_TEXTURE_2D, 0, cameraBuffer.getTextureID());

        pt.addUniform("u_Texture", camLoc);
        pt.addUniform("u_BufferTexture", bufferTextureData);

        minMaxFirstShader.addUniform("u_Texture", camLoc);
        minMaxFirstShader.addUniform("u_BufferTexture", bufferTextureData);
        minMaxFirstShader.addUniform("u_Delta", deltaData);

        minMaxSecondShader.addUniform("u_Texture", bufferTextureData);
        minMaxSecondShader.addUniform("u_Delta", deltaData);

        FloatData fadeAmount = new FloatData(.8f);
        minMaxSecondShader.addUniform("u_FadeAmount", fadeAmount);

        pt.addUniform("u_FadeAmount", fadeAmount);
    }

    private final Shader cameraToTextureShader, minMaxFirstShader, minMaxSecondShader;
    private FrameBuffer frontBuffer, backBuffer;
    private final FrameBuffer cameraBuffer;
    private final TextureLocationData
            bufferTextureData;
    private final Vector2Data deltaData;

}
