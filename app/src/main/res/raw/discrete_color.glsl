/*
 * Copyright (C) 2017  Taylor Jackle Spriggs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

void discrete(inout float target, const in int number) {
    target -= mod(target, 1./float(number));
    target *= float(number)/float(number-1);
}

void computeColor(out vec3 color, in vec2 fragCoord) {
    const int resolution = 256;
    const int sampling = 2;
    const float loopScale = 1./float(sampling*resolution);
    const float sumScale = 1./float(sampling*sampling);
    vec3 sum;
    vec2 coord;
    int i,j;
    discrete(fragCoord.x, resolution);
    discrete(fragCoord.y, resolution);
    for(i = 0; i < sampling; ++i) {
        for(j = 0; j < sampling; ++j) {
            coord = fragCoord+vec2(float(i)*loopScale,
                                    float(j)*loopScale);
            getTextureFragment(color, coord);
            sum += color*sumScale;
        }
    }
    color = sum;
    discrete(color.r, 8);
    discrete(color.g, 8);
    discrete(color.b, 4);
}