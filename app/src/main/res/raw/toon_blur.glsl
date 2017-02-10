
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

uniform sampler2D u_Edges;

uniform vec2 u_Delta;

void calcCol(inout vec4 color, inout float count, in vec2 texCoord) {
    vec4 frag;
    getTextureFragment(frag, texCoord);
    if(frag.a == 0.) {
        color.rgb += frag.rgb;
        color.a = max(color.a, frag.a);
        count += 1.;
    }
}

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    vec4 frag;
    float count = 0.;
    color = vec4(0.);
    calcCol(color, count, texCoord);
    calcCol(color, count, texCoord+u_Delta);
    calcCol(color, count, texCoord-u_Delta);
    if(count > 1.) {
        color.rgb /= count;
    }
}