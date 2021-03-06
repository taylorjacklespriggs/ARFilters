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

import static com.arfilters.GLTools.FrameBuffer;
import com.arfilters.shader.Shader;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;

/**
 * This operation renders the camera to a texture before rendering to each eye
 */
class RTTOperation extends BufferedOperation {

    @Override
    protected void renderToBuffers() {
        frameBuffer.enable();
        rttShader.draw();
    }

    protected RTTOperation(Shader rtt, Shader pt, FrameBuffer fb,
                           Matrix3x3Data vertMatData,
                           VertexMatrixUpdater ptVmi, String name) {
        super(pt, vertMatData, ptVmi, name);
        rttShader = rtt;
        frameBuffer = fb;
        pt.addUniform("u_Texture", new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 0, fb.getTextureID()));
    }

    private final Shader rttShader;
    private FrameBuffer frameBuffer;

}
