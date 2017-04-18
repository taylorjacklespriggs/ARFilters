
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
 *  Does not modify input color.
 */

uniform float u_Threshold;

#define FIRST_T .1
#define FIRST_V .0

#define SECOND_T .3
#define SECOND_V .3

#define THIRD_T .8
#define THIRD_V .6

#define FINAL_V 1.

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);
    float edge =
        length(vec2(dFdx(color.r), dFdy(color.r))) +
        length(vec2(dFdx(color.g), dFdy(color.g))) +
        length(vec2(dFdx(color.b), dFdy(color.b)));
    edge /= u_Threshold;
    float I = dot(color.rgb, vec3(1./3.));
    if(I > 0.) {
        float F;
        if(I < FIRST_T) {
            F = FIRST_V;
        } else if(I < SECOND_T) {
            F = SECOND_V;
        } else if(I < THIRD_T) {
            F = THIRD_V;
        } else {
            F = FINAL_V;
        }
        color.rgb *= F/I;
        color.rgb -= vec3(edge);
    }
}