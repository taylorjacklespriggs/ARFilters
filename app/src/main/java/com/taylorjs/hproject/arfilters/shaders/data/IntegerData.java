package com.taylorjs.hproject.arfilters.shaders.data;

import android.opengl.GLES20;

/**
 * Created by taylor on 1/20/17.
 */


public class IntegerData extends ShaderData {
    public int value;
    public IntegerData(int val) {
        value = val;
    }
    public void update(int location) {
        GLES20.glUniform1i(location, value);
    }
}

