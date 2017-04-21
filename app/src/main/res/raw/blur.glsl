
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
 *  Computes a 7-width gaussian blur along one axis with sigma=1.5
 *  Credit http://dev.theomader.com/gaussian-kernel-calculator/
 */

uniform vec2 u_Delta;

void computeColor(out vec4 color, in vec2 texCoord) {
    vec4 frag;
    const float gKernel0 = 0.266346,
                gKernel1 = 0.215007,
                gKernel2 = 0.113085,
                gKernel3 = 0.038735;
    getTextureFragment(frag, texCoord);
    color = frag*gKernel0;
    getTextureFragment(frag, texCoord+u_Delta);
    color += frag*gKernel1;
    getTextureFragment(frag, texCoord-u_Delta);
    color += frag*gKernel1;
    getTextureFragment(frag, texCoord+2.*u_Delta);
    color += frag*gKernel2;
    getTextureFragment(frag, texCoord-2.*u_Delta);
    color += frag*gKernel2;
    getTextureFragment(frag, texCoord+3.*u_Delta);
    color += frag*gKernel3;
    getTextureFragment(frag, texCoord-3.*u_Delta);
    color += frag*gKernel3;
}