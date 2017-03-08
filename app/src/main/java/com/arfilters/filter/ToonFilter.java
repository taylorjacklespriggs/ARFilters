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

class ToonFilter extends BufferedFilter {

    static ToonFilter create(ShaderGenerator camGen,
                             ShaderGenerator texGen,
                             FrameBuffer front,
                             FrameBuffer back,
                             int iters,
                             float threshold,
                             int lower,
                             Matrix3x3Data vertMatData,
                             VertexMatrixUpdater ptVmi) {

        // generate edge shader
        camGen.setComputeColor(R.raw.toon_edges);
        camGen.setUseDerivatives(true);
        Shader edge = camGen.generateShader();

        // generate blur shader
        texGen.setComputeColor(R.raw.toon_blur);
        Shader blur = texGen.generateShader();

        // create the passThrough shader
        texGen.setComputeColor(R.raw.toon_passthrough);
        Shader pt = texGen.generateShader();

        return new ToonFilter(edge, blur, pt, front, back,
                iters, threshold, lower, vertMatData, ptVmi);
    }

    @Override
    protected void renderToBuffers() {

        // render camera to texture
        firstBuffer.enable();

        edgeShader.draw();

        for(int i = 0; i < iterations; ++i) {

            // do horizontal blur
            bufferTextureData.newTextureLocation(firstBuffer.getTextureID());
            deltaData.updateData(new float[] {1f/firstBuffer.getWidth(), 0f});
            lowerData.updateData(0f);
            alphaScaleData.updateData(1f);

            secondBuffer.enable();
            blurShader.draw();

            // do vertical blur
            bufferTextureData.newTextureLocation(secondBuffer.getTextureID());
            deltaData.updateData(new float[] {0f, 1f/secondBuffer.getHeight()});
            lowerData.updateData(lowerNumber/255f);
            alphaScaleData.updateData(1f/lowerNumber);

            firstBuffer.enable();
            blurShader.draw();

        }

        // update bufferTextureID for passThrough
        bufferTextureData.newTextureLocation(firstBuffer.getTextureID());

    }

    private ToonFilter(Shader edge, Shader blur, Shader pt, FrameBuffer front,
                       FrameBuffer back, int iters, float threshold, int lower,
                       Matrix3x3Data vertMatData, VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi);
        edgeShader = edge;
        blurShader = blur;
        firstBuffer = front;
        secondBuffer = back;
        iterations = iters;

        bufferTextureData = new TextureLocationData(GLES20.GL_TEXTURE_2D, 0,
                firstBuffer.getTextureID());

        edgeShader.addUniform("u_Texture", bufferTextureData);
        edgeShader.addUniform("u_Threshold", new FloatData(threshold));

        pt.addUniform("u_Texture", bufferTextureData);

        deltaData = new Vector2Data();

        blurShader.addUniform("u_Texture", bufferTextureData);
        blurShader.addUniform("u_Delta", deltaData);

        lowerNumber = lower;
        blurShader.addUniform("u_Lower", lowerData = new FloatData(0f));
        blurShader.addUniform("u_AlphaScale", alphaScaleData = new FloatData(1f));
    }

    private final Shader edgeShader, blurShader;
    private FrameBuffer firstBuffer, secondBuffer;
    private final TextureLocationData
            bufferTextureData;

    private final FloatData lowerData, alphaScaleData;
    private final int lowerNumber;

    private Vector2Data deltaData;

    private int iterations;

}
