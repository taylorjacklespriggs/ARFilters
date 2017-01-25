package com.arfilters.shader.variable;

import com.arfilters.GLTools;
import com.arfilters.shader.data.ShaderData;

/**
 * Created by taylor on 1/20/17.
 */


public abstract class ShaderVariable {

    public static final String TAG = "ShaderVariable";

    public String getName() {
        return name;
    }

    public void update() {
        data.updateLocation(location);
        GLTools.checkGLError(TAG, "error updating "+getName());
    }

    protected abstract int getLocation(int program, String name);

    public ShaderVariable(String varName, int prog, ShaderData dat) {
        name = varName;
        location = getLocation(prog, name);
        data = dat;
    }

    protected final int location;
    private final String name;
    private final ShaderData data;

}

