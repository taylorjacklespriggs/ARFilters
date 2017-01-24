package com.arfilters.shader.data;

/**
 * Created by taylor on 1/20/17.
 */

public abstract class ShaderData<DataType> {

    public abstract void updateData(DataType dt);

    public abstract void updateLocation(int location);

    public void enable(int location) {}

    public void disable(int location) {}

}
