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

import com.arfilters.GLTools;
import com.arfilters.shader.Shader;
import com.arfilters.shader.data.Matrix3x3Data;

abstract class BufferedFilter extends SingleShaderFilter {

    private static final float[] IDENTITY = new float[] {
            1f,0f,0f,0f,1f,0f,0f,0f,1f
    };

    BufferedFilter(Shader pt, Matrix3x3Data vertMatData,
                          VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi);
    }

    @Override
    public final void prepareView() {

        vertexMatrixData.updateData(IDENTITY);

        int mainBuffer = GLTools.getCurrentFramebuffer();

        renderToBuffers();

        GLTools.setFramebuffer(mainBuffer);

    }

    protected abstract void renderToBuffers();
}
