
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

uniform float u_Scale;
uniform float u_VarScale;

void getGray(out highp float varOut, in vec2 varIn) {
    varOut = varIn.r*255.;
    varOut += varIn.g*255./256.;
}

float erf(float x) {
    const float
        a1 = 0.278393,
        a2 = 0.230389,
        a3 = 0.000972,
        a4 = 0.078108;
    float r = 1., x0 = x;
    r += a1*x;
    x *= x0;
    r += a2*x;
    x *= x0;
    r += a3*x;
    x *= x0;
    return 1.-1./pow(r + a4*x, 4.);
}

void sigmoid(inout highp float gray, in highp float avg, in highp float var) {
    gray = .5+.5*erf(0.7071068*(gray-avg)/sqrt(var));
}

void computeColor(out vec4 color, in vec2 texCoord) {
    getTextureFragment(color, texCoord);

    vec4 noise = texture2D(u_BufferTexture, texCoord);

    highp float gray, avg, var;
    getGray(gray, color.rg);
    getGray(avg, noise.rg);
    avg *= u_Scale;
    getGray(var, noise.ba);
    var *= u_VarScale;
    sigmoid(gray, avg, var);
    color.rgb = vec3(gray);
}