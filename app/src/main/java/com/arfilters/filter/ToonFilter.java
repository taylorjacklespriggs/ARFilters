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
import com.arfilters.shader.data.Vector2Data;

import static com.arfilters.GLTools.FrameBuffer;

public class ToonFilter extends SingleShaderFilter {

    private static final float[] IDENTITY = new float[] {
            1f,0f,0f,0f,1f,0f,0f,0f,1f
    };

    @Override
    public void prepareView() {

        vertexMatrixData.updateData(IDENTITY);

        // render camera to texture
        firstBuffer.enable();
        int oldBuffer = firstBuffer.getOldFramebuffer();

        edgeShader.draw();

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

        // reset original framebufferID
        firstBuffer.setOldFramebuffer(oldBuffer);
        firstBuffer.disable();

    }

    public ToonFilter(Shader edge, Shader blur, Shader pt,
                      FrameBuffer front,
                      FrameBuffer back,
                      int iters,
                      float threshold,
                      Matrix3x3Data vertMatData,
                      VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi);
        edgeShader = edge;
        blurShader = blur;
        firstBuffer = front;
        secondBuffer = back;
        iterations = iters;

        bufferTextureData = new TextureLocationData(GLES20.GL_TEXTURE_2D, 0, firstBuffer.getTextureID());

        edgeShader.addUniform("u_Texture", bufferTextureData);
        edgeShader.addUniform("u_Threshold", new FloatData(threshold));

        pt.addUniform("u_Texture", bufferTextureData);

        deltaData = new Vector2Data();

        blurShader.addUniform("u_Texture", bufferTextureData);
        blurShader.addUniform("u_Delta", deltaData);
    }

    private final Shader edgeShader, blurShader;
    private FrameBuffer firstBuffer, secondBuffer;
    private final TextureLocationData
            bufferTextureData;

    private Vector2Data deltaData;

    private int iterations;

}
