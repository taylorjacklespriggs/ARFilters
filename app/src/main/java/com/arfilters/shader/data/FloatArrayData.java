package com.arfilters.shader.data;

import android.opengl.GLES20;

/**
 * Created by taylor on 1/24/17.
 */

public class FloatArrayData extends FloatBufferData {

    @Override
    public void updateLocation(int location) {
        GLES20.glGetUniformfv(location, 1, buffer);
    }

    public FloatArrayData(int length) {
        super(length);
    }

}
