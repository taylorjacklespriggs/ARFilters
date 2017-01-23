package com.arfilters.shaders.data;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */

public abstract class ShaderData {
    public abstract void update(int location);
    public void enable(int location) {}
    public void disable(int location) {}

}
