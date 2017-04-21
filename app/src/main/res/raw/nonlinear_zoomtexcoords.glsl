
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
 * Modifies the texture coordinates for a zoomed-in effect in the center.
 */

varying vec2 v_TexCoord;

void transform(inout vec2 coord) {
    const float a = 4.;
    const float b = 6.;
    const float c = .01;
    const float rta = sqrt(a);
    const float rtc = sqrt(c);
    float r = length(coord);
    if(r > 0.) {
        coord *= 1./c-(a-c)*atan(b*rtc*r/rta)/(rta*b*c*rtc*r);
    }
}


void getTextureCoordinates(out vec2 texCoord) {
    texCoord = v_TexCoord;
    texCoord -= .5;
    transform(texCoord);
    texCoord += .5;
}
