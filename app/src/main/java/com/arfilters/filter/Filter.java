/*
 *  Copyright (C) 2017  Taylor Jackle Spriggs
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
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

import com.arfilters.shader.Viewinfo;

public abstract class Filter {

    public void draw(Viewinfo vi) {
        float scale = .6f;
        float Cw = scale*1920;
        float Ch = scale*1080;
        float Vw = vi.getEye().getViewport().width;
        float Vh = vi.getEye().getViewport().height;

        float[] texTransformMat = new float[] {
                Vw/Cw,          0,              0,
                0,              Vh/Ch,          0,
                (1f-Vw/Cw)/2f,  (1f-Vh/Ch)/2f,  1
        };
        vi.updateTextureTransformationMatrix(texTransformMat);
    }

}
