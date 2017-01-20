package com.taylorjs.hproject.arfilters.shaders.data;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

/**
 * Created by taylor on 1/20/17.
 */


public class TextureLocationData extends ShaderData {
    public final int glParameter;
    public int textureNumber;
    public int textureLocation;
    public TextureLocationData(int glParm, int texNum, int texLoc) {
        glParameter = glParm;
        textureNumber = texNum;
        textureLocation = texLoc;
    }
    public void update(int location) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureLocation);
        GLES20.glUniform1i(location, textureNumber);
    }
}
