package com.arfilters.shaders.data;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */


public class Matrix3x3Data extends FloatBufferData {
    public Matrix3x3Data(FloatBuffer fb) {
        super(fb);
    }
    @Override
    public void update(int location) {
        GLES20.glUniformMatrix3fv(location, 1, false, buffer);
    }
}

