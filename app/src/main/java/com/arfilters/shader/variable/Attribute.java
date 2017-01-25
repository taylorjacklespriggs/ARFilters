package com.arfilters.shader.variable;

import android.opengl.GLES20;

import com.arfilters.shader.data.AttributeData;
import com.arfilters.shader.data.ShaderData;

/**
 * Created by taylor on 1/20/17.
 */


public class Attribute extends ShaderVariable {

    @Override
    protected int getLocation(int program, String name) {
        return GLES20.glGetAttribLocation(program, name);
    }

    public void enable() {
        attributeData.enable(location);
    }

    public void disable() {
        attributeData.disable(location);
    }

    public Attribute(String name, int prog, ShaderData data, AttributeData aData) {
        super(name, prog, data);
        attributeData = aData;
    }

    private AttributeData attributeData;

}

