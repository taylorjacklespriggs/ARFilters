
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

/**
 *  Applies the histogram u_CDF to each channel and displays the sample window
 *  by slightly greying out the surrounding area
 */

uniform sampler2D u_CDF;
uniform float u_WindowScale;

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    color.r = texture2D(u_CDF, vec2(color.r, 0.)).r;
    color.g = texture2D(u_CDF, vec2(color.g, 0.)).g;
    color.b = texture2D(u_CDF, vec2(color.b, 0.)).b;
    texCoord *= 2.;
    texCoord -= vec2(1.);
    if(texCoord.x < -u_WindowScale || texCoord.y < -u_WindowScale
        || texCoord.x > u_WindowScale || texCoord.y > u_WindowScale) {
        color.rgb *= .9;
    }
}