package com.arfilters.shader.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by taylor on 1/20/17.
 */

public abstract class FloatBufferData extends ShaderData<float[]> {

    @Override
    public void updateData(float[] vals) {
        assert(vals.length == length);
        buffer.put(vals);
        buffer.position(0);
    }

    public FloatBufferData(int count) {
        length = count;
        ByteBuffer bb = ByteBuffer.allocateDirect(length*4);
        bb.order(ByteOrder.nativeOrder());
        buffer = bb.asFloatBuffer();
    }

    protected final FloatBuffer buffer;
    public final int length;

}
