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

public class FloatData implements ShaderData<Float> {

    private static final String TAG = "FloatData";

    @Override
    public synchronized void updateData(Float val) {
        value = val;
    }

    @Override
    public synchronized void updateLocation(int location) {
        GLES20.glUniform1f(location, value);
    }

    public FloatData(float val) {
        value = val;
    }

    private float value;

}

