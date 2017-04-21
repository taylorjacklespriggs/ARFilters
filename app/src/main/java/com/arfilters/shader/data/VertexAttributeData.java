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

package com.arfilters.shader.data;

import android.opengl.GLES20;
import android.util.Log;

import java.text.MessageFormat;

/**
 * This class enables special handling of vertex attributes
 */
public class VertexAttributeData extends FloatBufferData {

    private static final String TAG = "VertexAttributeData";

    @Override
    public synchronized void updateLocation(int location) {
        GLES20.glVertexAttribPointer(
                location, dimensions, GLES20.GL_FLOAT, false, 0, buffer);
    }

    public void enable(int location) {
        GLES20.glEnableVertexAttribArray(location);
    }

    public void disable(int location) {
        GLES20.glDisableVertexAttribArray(location);
    }

    public VertexAttributeData(int dim, int len) {
        super(dim*len);
        dimensions = dim;
    }

    private final int dimensions;

}

