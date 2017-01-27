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

package com.arfilters;

/**
 * Contains vertex, normal and color data.
 */
public class VertexData {

    public static final int FACE_COORD_DIMENSION = 3;
    public static final float[] FACE_COORDS = new float[] {
          -1.0f, -1.0f, 1.0f,
          -1.0f, 1.0f, 1.0f,
          1.0f, -1.0f, 1.0f,
          1.0f, 1.0f, 1.0f,
    };

    public static final int FACE_TEX_COORD_DIMENSION = 2;
    public static final float[] FACE_TEX_COORDS = new float[] {
          0.0f, 1.0f,
          0.0f, 0.0f,
          1.0f, 1.0f,
          1.0f, 0.0f,
    };

    public static final int FACE_NUMBER_VERTICES = FACE_COORDS.length/FACE_COORD_DIMENSION;
}
