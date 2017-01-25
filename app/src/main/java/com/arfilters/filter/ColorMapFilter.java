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

public class ColorMapFilter extends SingleShaderFilter {

    public void updateColorMap(float[] colorMap) {
        colorMapData.updateData(colorMap);
    }

    public ColorMapFilter(Shader sh) {
        super(sh);
        init("u_ColorMapMatrix", null);
    }

    public ColorMapFilter(Shader sh, String uName) {
        super(sh);
        init(uName, null);
    }

    public ColorMapFilter(Shader sh, float[] data) {
        super(sh);
        init("u_ColorMapMatrix", data);
    }

    public ColorMapFilter(Shader sh, String uName, float[] data) {
        super(sh);
        init(uName, data);
    }

    private void init(String uName, float[] data) {
        colorMapData = (data == null) ? new Matrix3x3Data() : new Matrix3x3Data(data);
        shader.addUniform(uName, colorMapData);
    }

    private Matrix3x3Data colorMapData;

}
