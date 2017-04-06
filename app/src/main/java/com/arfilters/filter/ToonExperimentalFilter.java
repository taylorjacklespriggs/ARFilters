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

class ToonExperimentalFilter extends BufferedFilter {

    static ToonExperimentalFilter create(ShaderGenerator camGen,
                                         ShaderGenerator texGen,
                                         FrameBuffer front,
                                         FrameBuffer back,
                                         int iters,
                                         FloatData strictness,
                                         FloatData threshold,
                                         float lower,
                                         Matrix3x3Data vertMatData,
                                         VertexMatrixUpdater ptVmi) {

        // generate blur shader
        camGen.setComputeColor(R.raw.blur);
        camGen.setUseDerivatives(false);
        Shader quickBlurHoriz = camGen.generateShader();

        texGen.setComputeColor(R.raw.blur);
        Shader quickBlurVert = texGen.generateShader();

        // generate edge shader
        texGen.setComputeColor(R.raw.toon_edges);
        texGen.setUseDerivatives(true);
        Shader edge = texGen.generateShader();

        // create the passThrough shader
        texGen.setComputeColor(R.raw.toon_experimental_passthrough);
        texGen.setUseDerivatives(false);
        Shader finalColor = texGen.generateShader();

        texGen.setComputeColor(R.raw.passthrough);
        Shader pt = texGen.generateShader();

        return new ToonExperimentalFilter(quickBlurHoriz, quickBlurVert, edge, finalColor, pt, front, back,
                iters, strictness, threshold, lower, vertMatData, ptVmi);
    }

    @Override
    protected void renderToBuffers() {

        // render camera to texture
        enableFrontBuffer();
        setHorizontalDelta();
        horizShader.draw();

        swapBuffers();
        setVerticalDelta();
        vertShader.draw();

        swapBuffers();
        edgeShader.draw();

        swapBuffers();
        finalShader.draw();

        // update bufferTextureID for passThrough
        useBufferTexture();

    }

    private void useBufferTexture() {
        bufferTextureData.newTextureLocation(firstBuffer.getTextureID());
    }

    private void enableFrontBuffer() {
        firstBuffer.enable();
    }

    private void swapBuffers() {
        useBufferTexture();
        FrameBuffer tmp = firstBuffer;
        firstBuffer = secondBuffer;
        secondBuffer = tmp;
        enableFrontBuffer();
    }

    private void setHorizontalDelta() {
        deltaData.updateData(new float[] {1f/firstBuffer.getWidth(), 0f});
    }

    private void setVerticalDelta() {
        deltaData.updateData(new float[] {0f, 1f/secondBuffer.getHeight()});
    }

    private ToonExperimentalFilter(Shader horiz, Shader vert, Shader edge, Shader col,
                                   Shader pt, FrameBuffer front,
                                   FrameBuffer back, int iters, FloatData strictness,
                                   FloatData threshold, float lower,
                                   Matrix3x3Data vertMatData, VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi, "Toon Experimental");
        edgeShader = edge;
        finalShader = col;
        firstBuffer = front;
        secondBuffer = back;
        iterations = iters;

        deltaData = new Vector2Data();

        horizShader = horiz;
        horizShader.addUniform("u_Delta", deltaData);

        bufferTextureData = new TextureLocationData(GLES20.GL_TEXTURE_2D, 0,
                firstBuffer.getTextureID());

        vertShader = vert;
        vertShader.addUniform("u_Texture", bufferTextureData);
        vertShader.addUniform("u_Delta", deltaData);

        edgeShader.addUniform("u_Texture", bufferTextureData);

        finalShader.addUniform("u_Texture", bufferTextureData);
        finalShader.addUniform("u_Threshold", threshold);

        pt.addUniform("u_Texture", bufferTextureData);

        lowerNumber = lower;
    }

    private final Shader horizShader, vertShader, edgeShader, finalShader;
    private FrameBuffer firstBuffer, secondBuffer;
    private final TextureLocationData
            bufferTextureData;

    private FloatData lowerData;
    private final float lowerNumber;

    private Vector2Data deltaData;

    private int iterations;

}
