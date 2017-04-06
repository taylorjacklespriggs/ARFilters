
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
 *  Computes a quick 3 width box blur in one dimension
 */

uniform vec2 u_Delta;

void computeColor(out vec4 color, in vec2 texCoord) {
    vec4 frag;
    getTextureFragment(frag, texCoord);
    color = frag;
    getTextureFragment(frag, texCoord+u_Delta);
    color += frag;
    getTextureFragment(frag, texCoord-u_Delta);
    color += frag;
    getTextureFragment(frag, texCoord+2.*u_Delta);
    color += frag;
    getTextureFragment(frag, texCoord-2.*u_Delta);
    color += frag;
    color /= 5.;
}