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

public class Vector4Data implements ShaderData<float[]> {

    private static final String TAG = "Vector2Data";

    @Override
    public synchronized void updateData(float[] vals) {
        x = vals[0];
        y = vals[1];
        z = vals[2];
        w = vals[3];
    }

    @Override
    public synchronized void updateLocation(int location) {
        GLES20.glUniform4f(location, x, y, z, w);
    }

    public Vector4Data() {
    }

    public Vector4Data(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    private float x, y, z, w;

}

