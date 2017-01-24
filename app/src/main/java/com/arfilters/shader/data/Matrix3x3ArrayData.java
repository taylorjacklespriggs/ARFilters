package com.arfilters.shader.data;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */


public class Matrix3x3ArrayData extends FloatBufferData {

    @Override
    public void updateLocation(int location) {
        GLES20.glUniformMatrix3fv(location, length, false, buffer);
    }

    public Matrix3x3ArrayData(int count) {
        super(count*9);
        length = count;
    }

    public final int length;

}

