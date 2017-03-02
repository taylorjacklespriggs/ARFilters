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
import com.arfilters.shader.data.Vector2Data;
import com.taylorjs.hproject.arfilters.R;

import static com.arfilters.GLTools.FrameBuffer;

class BlurFilter extends BufferedFilter {

    static BlurFilter create(ShaderGenerator camGen,
                                    ShaderGenerator texGen,
                                    FrameBuffer front,
                                    FrameBuffer back,
                                    int iters,
                                    Matrix3x3Data vertMatData,
                                    VertexMatrixUpdater ptVmi) {
        Shader ctt = camGen.generateShader();

        // create the passThrough shader
        Shader pt = texGen.generateShader();

        texGen.setComputeColor(R.raw.blur);
        Shader blur = texGen.generateShader();
        return new BlurFilter(ctt, blur, pt, front, back, iters, vertMatData,
                ptVmi);
    }

    @Override
    protected void renderToBuffers() {

        // render camera to texture
        firstBuffer.enable();

        cameraToTextureShader.draw();

        for(int i = 0; i < iterations; ++i) {

            // do horizontal blur
            bufferTextureData.newTextureLocation(firstBuffer.getTextureID());
            deltaData.updateData(new float[] {1f/firstBuffer.getWidth(), 0f});

            secondBuffer.enable();
            blurShader.draw();

            // do vertical blur
            bufferTextureData.newTextureLocation(secondBuffer.getTextureID());
            deltaData.updateData(new float[] {0f, 1f/secondBuffer.getHeight()});

            firstBuffer.enable();
            blurShader.draw();

        }

        // update bufferTextureID for passThrough
        bufferTextureData.newTextureLocation(firstBuffer.getTextureID());

    }

    private BlurFilter(Shader ctt, Shader blur, Shader pt, FrameBuffer front,
               FrameBuffer back, int iters, Matrix3x3Data vertMatData,
               VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi);
        cameraToTextureShader = ctt;
        blurShader = blur;
        firstBuffer = front;
        secondBuffer = back;
        iterations = iters;

        bufferTextureData = new TextureLocationData(GLES20.GL_TEXTURE_2D, 0, firstBuffer.getTextureID());

        pt.addUniform("u_Texture", bufferTextureData);

        deltaData = new Vector2Data();

        blurShader.addUniform("u_Texture", bufferTextureData);
        blurShader.addUniform("u_Delta", deltaData);
    }

    private final Shader cameraToTextureShader, blurShader;
    private FrameBuffer firstBuffer, secondBuffer;
    private final TextureLocationData
            bufferTextureData;

    private Vector2Data deltaData;

    private int iterations;

}
