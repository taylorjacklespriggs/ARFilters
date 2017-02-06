
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
 *  Mix alternate texture with original in one color using red and green channels
 *  for higher precision. Could not render to one 16bit component with GLES20.
 */

 void setGray(out vec2 varOut, in highp float varIn) {
    highp float g = mod(varIn, 1.);
    varOut.g = g*256./255.;
    varOut.r = (varIn-g)/255.;
 }

 void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    setGray(color.rg, dot(color.rgb, vec3(1.)));
 }