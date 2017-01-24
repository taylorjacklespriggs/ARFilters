
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
 *  Computes the edges of the greyscale.
 */

uniform float u_Threshold;
uniform float u_Strictness;

void sigmoid(out float varOut, in float varIn) {
    varOut = 1./(1.+exp(-varIn));
}

void computeColor(out vec3 color, in vec2 fragCoord) {
    getTextureFragment(color, fragCoord);
    float grey = length(color);
    grey = length(vec2(dFdx(grey), dFdy(grey)));
    sigmoid(grey, u_Strictness*(grey-u_Threshold));
    color = vec3(grey);
}