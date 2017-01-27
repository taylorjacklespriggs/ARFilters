package com.arfilters.shader.variable;

import com.arfilters.GLTools;
import com.arfilters.shader.data.ShaderData;

public abstract class ShaderVariable {

    private static final String TAG = "ShaderVariable";

    public String getName() {
        return name;
    }

    public void update() {
        data.updateLocation(location);
        GLTools.checkGLError(TAG, "error updating "+getName());
    }

    protected abstract int getLocation(int program, String name);

    ShaderVariable(String varName, int prog, ShaderData dat) {
        name = varName;
        location = getLocation(prog, name);
        data = dat;
    }

    final int location;
    private final String name;
    private final ShaderData data;

}

