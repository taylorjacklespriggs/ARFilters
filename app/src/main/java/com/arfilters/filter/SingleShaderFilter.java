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
import com.arfilters.shader.Viewinfo;

public class SingleShaderFilter extends Filter {

    public SingleShaderFilter(Shader sh) {
        shader = sh;
    }

    @Override
    public void draw(Viewinfo vi) {
        super.draw(vi);
        shader.draw();
    }

    protected final Shader shader;

}
