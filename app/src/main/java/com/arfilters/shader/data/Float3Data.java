package com.arfilters.shader.data;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */


public class Float3Data extends FloatBufferData {

    @Override
    protected void doUniformUpdate(int location) {
        GLES20.glUniform3fv(location, 1, buffer);
    }

    public Float3Data(int sz) {
        super(sz);
    }

}

