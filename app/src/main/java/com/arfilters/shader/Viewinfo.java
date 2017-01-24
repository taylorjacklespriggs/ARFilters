package com.arfilters.shader;

import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.variable.Uniform;

/**
 * Created by taylor on 1/23/17.
 */

public class Viewinfo {

    public void updateTextureTransformationMatrix(Matrix3x3Data m3x3) {
        m3x3.updateData(textureTransformMatrix);
    }

    public Viewinfo(float[] texTrans) {
        textureTransformMatrix = texTrans;
    }

    private float[] textureTransformMatrix;

}
