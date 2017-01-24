package com.arfilters.shader.filter;

import com.arfilters.shader.Shader;

/**
 * Created by taylor on 1/23/17.
 */

public abstract class SingleShaderFilter implements Filter {
    protected final Shader shader;
    public SingleShaderFilter(Shader shade) {
        shader = shade;
    }
}
