
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
 *  All final pass fragment shader programs should use this main() and implement
 *  computeTextureCoordinates(out vec2) and computeColor(out vec3, in vec2).
 *  This reduces reused code.
 */

void main() {
    vec2 texCoord;
    getTextureCoordinates(texCoord);
    computeColor(gl_FragColor.rgb, texCoord);
}
