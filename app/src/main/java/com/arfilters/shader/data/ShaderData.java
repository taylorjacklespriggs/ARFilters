package com.arfilters.shader.data;

/**
 * Created by taylor on 1/20/17.
 */

public abstract class ShaderData<DataType> {

    protected abstract void doDataUpdate(DataType dt);

    protected abstract void doUniformUpdate(int loc);

    public final void updateData(DataType dt) {
        modified = true;
        doDataUpdate(dt);
    }

    public final void updateLocation(int location) {
        doUniformUpdate(location);
        modified = false;
    }

    public void reset() {
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    private boolean modified = true;

}
