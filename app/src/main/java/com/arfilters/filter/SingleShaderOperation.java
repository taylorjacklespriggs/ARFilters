/*
 *  Copyright (C) 2017  Taylor Jackle Spriggs
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.arfilters.filter;

import com.arfilters.shader.Shader;
import com.arfilters.shader.data.Matrix3x3Data;
import com.google.vr.sdk.base.Eye;

/**
 * This operation executes a single shader operation on each eye
 */
class SingleShaderOperation implements ImageOperation {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void prepareView() {
    }

    @Override
    public void drawEye(Eye eye) {
        vertexMatrixData.updateData(vertMatUpdater.updateVertexMatrix(eye));
        shader.draw();
    }

    @Override
    public void cleanup() {}

    SingleShaderOperation(Shader sh,
                          Matrix3x3Data vertMatData,
                          VertexMatrixUpdater vmi,
                          String nm) {
        shader = sh;
        vertexMatrixData = vertMatData;
        vertMatUpdater = vmi;
        name = nm;
    }

    private final Shader shader;
    final Matrix3x3Data vertexMatrixData;
    private final VertexMatrixUpdater vertMatUpdater;
    private final String name;

}
