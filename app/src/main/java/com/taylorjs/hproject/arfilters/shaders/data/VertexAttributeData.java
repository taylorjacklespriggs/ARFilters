package com.taylorjs.hproject.arfilters.shaders.data;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */


public class VertexAttributeData extends FloatBufferData {
    public final int dimensions;
    public VertexAttributeData(int dim, FloatBuffer fb) {
        super(fb);
        dimensions = dim;
    }
    @Override
    public void update(int location) {
        GLES20.glVertexAttribPointer(
                location, dimensions, GLES20.GL_FLOAT, false, 0, buffer);
    }
    @Override
    public void enable(int location) {
        GLES20.glEnableVertexAttribArray(location);
    }
    @Override
    public void disable(int location) {
        GLES20.glDisableVertexAttribArray(location);
    }
}

