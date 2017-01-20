package com.taylorjs.hproject.arfilters.shaders.data;

import android.opengl.GLES20;

/**
 * Created by taylor on 1/20/17.
 */


public class FloatData extends ShaderData {
    public float value;
    public FloatData(float val) {
        value = val;
    }
    public void update(int location) {
        GLES20.glUniform1f(location, value);
    }
}

