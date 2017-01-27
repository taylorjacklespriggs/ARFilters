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

import com.arfilters.shader.ViewInfo;

class HueRotationFilter implements Filter {

    @Override
    public void draw(ViewInfo vi) {
        colorMapFilter.updateColorMap(
                computeMatrix((float)((count++)*2*Math.PI/loopFrames)));
        colorMapFilter.draw(vi);
    }

    private ColorMapFilter colorMapFilter;
    private int count;
    private final int loopFrames;

    HueRotationFilter(ColorMapFilter cmf, int loop) {
        colorMapFilter = cmf;
        count = 0;
        loopFrames = loop;
    }

    private float[] computeMatrix(float hueAngle) {
        float lr, lg, lb, a, b, c, cos, sin;
        lr=0.213f;
        lg=0.715f;
        lb=0.072f;
        a=0.143f;
        b=0.140f;
        c=-0.283f;
        cos=(float)Math.cos(hueAngle);
        sin=(float)Math.sin(hueAngle);
        return new float[]{

                lr + cos * (1 - lr) + sin * (-lr),
                lg + cos * (-lg) + sin * (-lg),
                lb + (float)Math.cos(-lb) + sin * (1 - lb),

                lr + cos * (-lr) + sin * (a),
                lg + cos * (1 - lg) + sin * (b),
                lb + (float)Math.cos(-lb) + sin * (c),

                lr + cos * (-lr) + sin * (-(1 - lr)),
                lg + cos * (-lg) + sin * (lg),
                lb + (float)Math.cos(1 - lb) + sin * (lb),

        };
    }
}
