package com.arfilters.shader.data;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */


public class VertexAttributeData extends FloatBufferData {

    @Override
    public void updateLocation(int location) {
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

    public VertexAttributeData(int dim, int len) {
        super(dim*len);
        dimensions = dim;
    }

    public final int dimensions;

}

