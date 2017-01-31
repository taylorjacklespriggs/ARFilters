package com.arfilters.shader.variable;

import android.opengl.GLES20;
import android.util.Log;

import com.arfilters.shader.data.VertexAttributeData;

import java.text.MessageFormat;

public class Attribute extends ShaderVariable {

    private static final String TAG = "Attribute";

    @Override
    protected int getLocation(int program, String name) {
        int location = GLES20.glGetAttribLocation(program, name);
        return location;
    }

    public void enable() {
        attributeData.enable(location);
    }

    public void disable() {
        attributeData.disable(location);
    }

    public Attribute(String name, int prog, VertexAttributeData aData) {
        super(name, prog, aData);
        attributeData = aData;
    }

    private VertexAttributeData attributeData;

}

