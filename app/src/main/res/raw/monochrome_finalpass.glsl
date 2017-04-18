
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
 *  Takes red to be MSB and green to be LSB of grayscale color.
 */

uniform sampler2D u_CDF;

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    highp float gray = color.r*255.;
    gray += color.g*255./256.;
    gray /= 256.;
    color.rgb = vec3(texture2D(u_CDF, vec2(gray, 0.)).a);
}