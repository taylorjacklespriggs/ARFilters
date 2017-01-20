package com.taylorjs.hproject.arfilters.shaders.data;

import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */

public abstract class FloatBufferData extends ShaderData {
    public FloatBuffer buffer;
    public FloatBufferData(FloatBuffer fb) {
        buffer = fb;
    }
}
