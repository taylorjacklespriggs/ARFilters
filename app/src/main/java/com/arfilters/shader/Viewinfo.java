/*
 *  Copyright (C) 2017  Taylor Jackle Spriggs
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.arfilters.shader;

import com.arfilters.shader.data.Matrix3x3Data;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;

public class Viewinfo {

    public void prepareShaderTextureTransformationMatrix(Shader sh, String transUniName) {
        sh.addUniform(transUniName, textureTransformationMatrixData);
    }

    public void updateTextureTransformationMatrix(float[] data) {
        textureTransformationMatrixData.updateData(data);
    }

    public HeadTransform getHeadTransform() {
        return headTransform;
    }

    public void setHeadTransform(HeadTransform ht) {
        headTransform = ht;
    }

    public Eye getEye() {
        return eye;
    }

    public void setEye(Eye e) {
        eye = e;
    }

    public Viewinfo() {
        init();
    }

    public Viewinfo(float[] texTrans) {
        init();
        updateTextureTransformationMatrix(texTrans);
    }

    private void init() {
        textureTransformationMatrixData = new Matrix3x3Data();
    }

    private Eye eye;
    private HeadTransform headTransform;
    private Matrix3x3Data textureTransformationMatrixData;

}
