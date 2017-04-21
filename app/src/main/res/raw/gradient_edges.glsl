
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


/**
 * Computes the edges for each color. Computes the length of the resulting
 * color and passes through a sigmoid.
 */

uniform float u_Threshold;
uniform float u_Strictness;

void sigmoid(inout float var) {
    var = u_Strictness*(var-u_Threshold);
    var = 1./(1.+exp(-var));
}

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    float gLength =
        length(vec2(dFdx(color.r), dFdy(color.r))) +
        length(vec2(dFdx(color.g), dFdy(color.g))) +
        length(vec2(dFdx(color.b), dFdy(color.b)));
    gLength /= 3.;
    sigmoid(gLength);
    color.rgb = vec3(gLength);
}