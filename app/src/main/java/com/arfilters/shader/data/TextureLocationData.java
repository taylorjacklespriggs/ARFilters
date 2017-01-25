package com.arfilters.shader.data;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

/**
 * Created by taylor on 1/20/17.
 */


public class TextureLocationData extends ShaderData<Integer> {

    @Override
    protected void doDataUpdate(Integer texNum) {
        textureNumber = texNum;
    }

    @Override
    protected void doUniformUpdate(int location) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureLocation);
        GLES20.glUniform1i(location, textureNumber);
    }

    public TextureLocationData(int glParm, int texNum, int texLoc) {
        glParameter = glParm;
        textureNumber = texNum;
        textureLocation = texLoc;
    }

    public final int glParameter;
    public int textureNumber;
    public int textureLocation;

}
