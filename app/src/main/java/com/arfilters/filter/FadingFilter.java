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

import com.arfilters.shader.Shader;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;

import static com.arfilters.GLTools.FrameBuffer;

public class FadingFilter extends SingleShaderFilter {

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

        // mix cameraBuffer and backBuffer
        frontBuffer.enable();
        backTextureData.newTextureLocation(backBuffer.getTextureID());
        mixingShader.draw();

        // update frontTextureID for passThrough
        frontTextureData.newTextureLocation(frontBuffer.getTextureID());

        // swap framebuffers
        FrameBuffer tmp = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = tmp;

        // reset original framebufferID
        cameraBuffer.disable();

    }

    public FadingFilter(Shader ctt, Shader mix, Shader pt, FrameBuffer front,
                        FrameBuffer back, FrameBuffer camera,
                        Matrix3x3Data vertMatData,
                        TextureLocationData frontTexture,
                        TextureLocationData backTexture,
                        VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi);
        cameraToTextureShader = ctt;
        mixingShader = mix;
        passThroughShader = pt;
        frontBuffer = front;
        backBuffer = back;
        cameraBuffer = camera;
        frontTextureData = frontTexture;
        backTextureData = backTexture;
    }

    private final Shader cameraToTextureShader, mixingShader, passThroughShader;
    private FrameBuffer frontBuffer, backBuffer;
    private final FrameBuffer cameraBuffer;
    private final TextureLocationData
            frontTextureData,
            backTextureData;

}
