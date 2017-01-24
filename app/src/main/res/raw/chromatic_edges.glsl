
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


/*
 *  Computes the edges for each color. Passes results through a scaled sigmoid.
 */

uniform float u_Threshold;
uniform float u_Strictness;

void sigmoid(out vec3 varOut, in vec3 varIn) {
    varOut = 1./(1.+exp(-varIn));
}

void computeColor(out vec3 color, in vec2 fragCoord) {
    getTextureFragment(color, fragCoord);
    color = vec3(
        length(vec2(dFdx(color.r), dFdy(color.r))),
        length(vec2(dFdx(color.g), dFdy(color.g))),
        length(vec2(dFdx(color.b), dFdy(color.b)))
    );
    sigmoid(color, u_Strictness*(color-u_Threshold));
}