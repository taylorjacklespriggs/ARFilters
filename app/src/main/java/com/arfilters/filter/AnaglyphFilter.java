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
import com.google.vr.sdk.base.Eye;

class AnaglyphFilter extends ColorMapFilter {

    @Override
    public void drawEye(ViewInfo vi) {
        updateColorMap((vi.getEyeType() == Eye.Type.LEFT) ? leftMap : rightMap);
        super.drawEye(vi);
    }

    AnaglyphFilter(Shader cMapShader, Matrix3x3Data vertMat,
                   VertexMatrixUpdater vmi, Matrix3x3Data colorMapMatrix,
                   float[] left, float[] right) {
        super(cMapShader, vertMat, vmi, colorMapMatrix);
        leftMap = left;
        rightMap = right;
    }

    private final float[] leftMap, rightMap;

}
