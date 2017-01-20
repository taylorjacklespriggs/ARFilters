package com.taylorjs.hproject.arfilters.shaders.variable;

import android.opengl.GLES20;

/**
 * Created by taylor on 1/20/17.
 */


public class Uniform extends ShaderVariable {

    @Override
    protected int getLocation(int program, String name) {
        return GLES20.glGetUniformLocation(program, name);
    }

    public Uniform(String name, int prog) {
        super(name, prog);
    }

}

