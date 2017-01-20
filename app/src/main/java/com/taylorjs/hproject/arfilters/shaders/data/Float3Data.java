package com.taylorjs.hproject.arfilters.shaders.data;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */


public class Float3Data extends FloatBufferData {
    public Float3Data(FloatBuffer fb) {
        super(fb);
    }
    @Override
    public void update(int location) {
        GLES20.glUniform3fv(location, 1, buffer);
    }
}

