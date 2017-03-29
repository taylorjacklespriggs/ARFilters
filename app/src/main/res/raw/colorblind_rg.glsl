
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
 *  A simple function that computes fragment color using a color mapping matrix. The function
 *  getFragment(vec2) should be specified.
 */

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    if(2.*color.b < min(color.r, color.g)) {
        if(color.r > color.g) {
            color.r += color.g;
            color.g = 0.;
        } else {
            color.g += color.r;
            color.r = 0.;
        }
    }
}