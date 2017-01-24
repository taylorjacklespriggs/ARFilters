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
        if(data != null) {
            data.updateLocation(location);
            GLTools.checkGLError(TAG, "error updating "+getName());
        }
    }

    public void enable() {
        if(data != null) {
            data.enable(location);
        }
    }

    public void disable() {
        if(data != null) {
            data.disable(location);
        }
    }

    public void setData(ShaderData sd) {
        data = sd;
    }

    public ShaderData getData() {
        return data;
    }

    public boolean hasData() {
        return data != null;
    }

    protected abstract int getLocation(int program, String name);

    public ShaderVariable(String varName, int prog, ShaderData dat) {
        name = varName;
        location = getLocation(prog, name);
        data = dat;
    }

    private int location;
    private String name;
    private ShaderData data;

}

