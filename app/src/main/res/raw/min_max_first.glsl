
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
 * First step of local contrast equalization. Finds maximum and minimum
 * neighbors in the buffer on one axis. Updates minimum and maximum with
 * intensity of the camera pixel. Buffer texture is stored as average and
 * radius, output is minimum and maximum.
 */

uniform sampler2D u_BufferTexture;
uniform vec2 u_Delta;

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

    highp float gray = dot(color.rgb, vec3(255./3.));

    highp vec2 thisPixel;
    getComponents(thisPixel, texture2D(u_BufferTexture, texCoord));
    thisPixel = vec2(thisPixel.x-thisPixel.y, thisPixel.x+thisPixel.y);

    highp vec2 nnbor, pnbor;
    getComponents(nnbor, texture2D(u_BufferTexture, texCoord+u_Delta));
    getComponents(pnbor, texture2D(u_BufferTexture, texCoord-u_Delta));

    thisPixel.x = min(thisPixel.x, gray);
    thisPixel.x = min(thisPixel.x, nnbor.x-nnbor.y);
    thisPixel.x = min(thisPixel.x, pnbor.x-pnbor.y);

    thisPixel.y = max(thisPixel.y, gray);
    thisPixel.y = max(thisPixel.y, nnbor.x+nnbor.y);
    thisPixel.y = max(thisPixel.y, pnbor.x+pnbor.y);

    setGray(color.rg, thisPixel.x);
    setGray(color.ba, thisPixel.y);
}