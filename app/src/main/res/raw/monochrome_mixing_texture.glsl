
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

 uniform sampler2D u_AlternateTexture;

 uniform float u_FadeAmount;

 void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    // red as MSB
    highp float gray1, gray2;
    gray1 = length(color.rgb);
    vec2 rg = texture2D(u_AlternateTexture, texCoord).rg;
    gray2 = 255.*rg.r;
    gray2 += rg.g*255./256.; // g in range [0,1] but should be in [0,255/256]
    // add gray values
    gray1 += u_FadeAmount*gray2;
    color.g = mod(gray1, 1.); // get LSB
    color.r = (gray1 - color.g)/255.; // get MSB
 }