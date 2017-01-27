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
import com.google.vr.sdk.base.Eye;

class AnaglyphFilter implements Filter {

    @Override
    public void draw(ViewInfo vi) {
        switch(vi.getViewType()) {
            case LEFT_EYE:
                colorMapFilter.updateColorMap(rightMap);
                break;
            default:
                colorMapFilter.updateColorMap(leftMap);
                break;
        }
        colorMapFilter.draw(vi);
    }

    AnaglyphFilter(ColorMapFilter cmf, float[] left, float[] right) {
        colorMapFilter = cmf;
        leftMap = left;
        rightMap = right;
    }

    private ColorMapFilter colorMapFilter;
    private float[] leftMap, rightMap;

}
