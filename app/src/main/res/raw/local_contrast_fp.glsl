
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

void getGray(out highp float varOut, in vec2 varIn) {
    varOut = varIn.r*255.;
    varOut += varIn.g*255./256.;
}

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);

    vec4 thisPixel = texture2D(u_BufferTexture, texCoord);

    highp float intensity, gray, mid, rad;
    intensity = dot(color.rgb, vec3(1./3.));
    if(intensity > 0.) {
        gray = 255.*intensity;

        getGray(mid, thisPixel.rg);
        getGray(rad, thisPixel.ba);
        rad /= u_FadeAmount;

        if(rad > 1./255.)
            color.rgb *= (gray-mid)*.5/(intensity*rad)+.5;
        else
            color.rgb *= .5/intensity;
    }
}