
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

 uniform sampler2D u_BufferTexture;

 uniform float u_FadeAmount;
 uniform float u_Scale;

 void getGray(out highp float varOut, in vec2 varIn) {
    varOut = varIn.r*255.;
    varOut += varIn.g*255./256.;
 }

 void setGray(out vec2 varOut, in highp float varIn) {
    highp float g = mod(varIn, 1.);
    varOut.g = g*256./255.;
    varOut.r = (varIn-g)/255.;
 }

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    vec4 noise = texture2D(u_BufferTexture, texCoord);

    highp float gray, avg, var;
    getGray(gray, color.rg);
    getGray(avg, noise.rg);
    getGray(var, noise.ba);

    setGray(color.rg, gray+u_FadeAmount*avg);
    setGray(color.ba, pow(gray-u_Scale*avg, 2.)+u_FadeAmount*var);
}