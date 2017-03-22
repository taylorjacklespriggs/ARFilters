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
import com.arfilters.shader.ViewInfo;
import com.arfilters.shader.data.Matrix3x3Data;

class ColorblindFilter extends ColorMapFilter {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void prepareView() {
        updateColorMap(colorMap);
    }

    ColorblindFilter(Shader sh,
                     Matrix3x3Data vertMatrix,
                     VertexMatrixUpdater vmi,
                     Matrix3x3Data colorMapMat,
                     float[] cbMap,
                     String nm) {
        super(sh, vertMatrix, vmi, colorMapMat, nm);
        colorMap = cbMap;
        name = nm;
    }

    private float[] colorMap;
    private final String name;
}
