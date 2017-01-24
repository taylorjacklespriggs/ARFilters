package com.arfilters.shader.data;

import android.opengl.GLES20;

/**
 * Created by taylor on 1/20/17.
 */


public class IntegerData extends ShaderData<Integer> {

    @Override
    public void updateData(Integer data) {
        value = data;
    }

    @Override
    public void updateLocation(int location) {
        GLES20.glUniform1i(location, value);
    }

    public IntegerData(int val) {
        value = val;
    }

    private int value;

}

