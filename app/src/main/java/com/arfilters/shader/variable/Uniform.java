package com.arfilters.shader.variable;

import android.opengl.GLES20;

import com.arfilters.shader.data.ShaderData;

public class Uniform extends ShaderVariable {

    @Override
    protected int getLocation(int program, String name) {
        return GLES20.glGetUniformLocation(program, name);
    }

    public Uniform(String name, int prog, ShaderData data) {
        super(name, prog, data);
    }

}

