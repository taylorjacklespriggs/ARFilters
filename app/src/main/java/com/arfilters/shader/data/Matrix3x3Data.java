package com.arfilters.shader.data;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */


public class Matrix3x3Data extends FloatBufferData {

    @Override
    public void updateLocation(int location) {
        GLES20.glUniformMatrix3fv(location, 1, false, buffer);
    }

    public Matrix3x3Data() {
        super(9);
    }

}

