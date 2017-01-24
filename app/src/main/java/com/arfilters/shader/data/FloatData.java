package com.arfilters.shader.data;

import android.opengl.GLES20;

/**
 * Created by taylor on 1/20/17.
 */


public class FloatData extends ShaderData<Float> {

    @Override
    public void updateData(Float val) {
        value = val;
    }

    @Override
    public void updateLocation(int location) {
        GLES20.glUniform1f(location, value);
    }

    public FloatData(float val) {
        value = val;
    }

    private float value;

}

