package com.taylorjs.hproject.arfilters.shaders.data;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */


public class Matrix3x3ArrayData extends FloatBufferData {
    int length;
    public Matrix3x3ArrayData(FloatBuffer fb, int count) {
        super(fb);
        length = count;
    }
    @Override
    public void update(int location) {
        GLES20.glUniformMatrix3fv(location, length, false, buffer);
    }
}

