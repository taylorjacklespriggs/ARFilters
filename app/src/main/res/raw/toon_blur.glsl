
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
 *  Computes a horizontal horiz_blur.
 */

uniform vec2 u_Delta;
uniform float u_Lower;

void calcCol(inout vec4 color, in vec2 texCoord) {
    vec4 frag;
    getTextureFragment(frag, texCoord);
    color += frag;
}

void computeColor(out vec4 color, in vec2 texCoord) {
    color = vec4(0.);
    calcCol(color, texCoord);
    calcCol(color, texCoord+u_Delta);
    calcCol(color, texCoord-u_Delta);
    calcCol(color, texCoord+2.*u_Delta);
    calcCol(color, texCoord-2.*u_Delta);
    color /= 5.;
    if(color.a < u_Lower) {
        color.a = 0.;
    }
}