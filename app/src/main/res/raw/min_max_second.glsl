
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

uniform vec2 u_Delta;
uniform float u_FadeAmount;

void getGray(out highp float varOut, in vec2 varIn) {
    varOut = varIn.r*255.;
    varOut += varIn.g*255./256.;
}

void setGray(out vec2 varOut, in highp float varIn) {
    highp float g = mod(varIn, 1.);
    varOut.g = g*256./255.;
    varOut.r = (varIn-g)/255.;
}

void getComponents(out highp vec2 comps, in vec4 varIn) {
    getGray(comps.x, varIn.rg);
    getGray(comps.y, varIn.ba);
}

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);

    highp vec2 thisPixel;
    getComponents(thisPixel, color);

    highp vec2 nnbor, pnbor;
    getTextureFragment(color, texCoord+u_Delta);
    getComponents(nnbor, color);
    getTextureFragment(color, texCoord-u_Delta);
    getComponents(pnbor, color);

    thisPixel.x = min(thisPixel.x, nnbor.x);
    thisPixel.x = min(thisPixel.x, pnbor.x);

    thisPixel.y = max(thisPixel.y, nnbor.y);
    thisPixel.y = max(thisPixel.y, pnbor.y);

    thisPixel.x += thisPixel.y;
    thisPixel.x *= .5;
    thisPixel.y -= thisPixel.x;

    thisPixel.y *= u_FadeAmount;

    setGray(color.rg, thisPixel.x);
    setGray(color.ba, thisPixel.y);
}