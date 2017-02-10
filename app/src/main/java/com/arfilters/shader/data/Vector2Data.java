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

public class Vector2Data implements ShaderData<float[]> {

    private static final String TAG = "Vector2Data";

    @Override
    public void updateData(float[] vals) {
        x = vals[0];
        y = vals[1];
    }

    @Override
    public void updateLocation(int location) {
        GLES20.glUniform2f(location, x, y);
    }

    public Vector2Data() {
    }

    public Vector2Data(float x, float y) {
        this.x = x;
        this.y = y;
    }

    private float x, y;

}

