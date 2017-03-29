
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
 *  Does not modify input color.
 */

uniform sampler2D u_CDF;
uniform vec2 u_BLCorner;
uniform vec2 u_TRCorner;

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    color.r = texture2D(u_CDF, vec2(color.r, 0.)).r;
    color.g = texture2D(u_CDF, vec2(color.g, 0.)).g;
    color.b = texture2D(u_CDF, vec2(color.b, 0.)).b;
    if(texCoord.x < u_BLCorner.x || texCoord.y < u_BLCorner.y
        || texCoord.x > u_TRCorner.x || texCoord.y > u_TRCorner.y) {
        color.rgb *= .9;
    }
}