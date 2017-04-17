
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
 *  This vertex shader computes the position with a vertex transformation matrix
 */

attribute vec3 a_Position;
attribute vec2 a_TexCoord;

uniform mat3 u_VertexTransform;

varying vec2 v_TexCoord;

void main() {
   v_TexCoord = a_TexCoord;
   vec3 pos = a_Position*u_VertexTransform;
   gl_Position = vec4(pos.xy, 0., pos.z);
}
