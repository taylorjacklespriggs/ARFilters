package com.arfilters.shader.variable;

import android.opengl.GLES20;
import android.util.Log;

import com.arfilters.shader.data.ShaderData;

import java.text.MessageFormat;

public class Uniform extends ShaderVariable {

    private static final String TAG = "Uniform";

    @Override
    protected int getLocation(int program, String name) {
        return GLES20.glGetUniformLocation(program, name);
    }

    public Uniform(String name, int prog, ShaderData data) {
        super(name, prog, data);
    }

}

