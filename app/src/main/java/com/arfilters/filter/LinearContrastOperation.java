/*
 * Copyright (C) 2017  Taylor Jackle Spriggs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.arfilters.filter;

import static com.arfilters.GLTools.FrameBuffer;
import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x3Data;
import com.taylorjs.hproject.arfilters.R;

/**
 * Applies a global linear contrast adjustment by regularly sampling frames for
 * minimum and maximum pixel intensities
 */
public class LinearContrastOperation extends ImageSampleOperation {

    private static final String TAG = LinearContrastOperation.class.getName();

    public static LinearContrastOperation create(ShaderGenerator camGen,
                                                 ShaderGenerator texGen,
                                                 FrameBuffer fb,
                                                 FrameBuffer sampler,
                                                 Matrix3x3Data vertMatData,
                                                 VertexMatrixUpdater ptVmi,
                                                 int updateFreq) {
        camGen.setComputeColor(R.raw.passthrough);
        Shader ctt = camGen.generateShader();
        texGen.setComputeColor(R.raw.passthrough);
        Shader samp = texGen.generateShader();
        texGen.setComputeColor(R.raw.linear_contrast);
        Shader contrast = texGen.generateShader();
        return new LinearContrastOperation(ctt, contrast, samp, fb, sampler,
                vertMatData, ptVmi, updateFreq, "Linear Contrast");
    }

    private class ContrastInfo implements ImageSampler {
        @Override
        public void feed(int x, int y, int r, int g, int b, int a) {
            int pix = r+g+b;
            if(minimum == null || pix < minimum)
                minimum = pix;
            if(maximum == null || maximum < pix)
                maximum = pix;
        }
        @Override
        public void finish() {
            float affine = -minimum/(255f*3);
            affineData.updateData(affine);
            scaleData.updateData(1f/(maximum/(255f*3)+affine));
        }
        Integer minimum, maximum;
    }

    @Override
    protected ImageSampler createImageSampler() {
        return new ContrastInfo();
    }

    private LinearContrastOperation(Shader rtt, Shader pt, Shader samp, FrameBuffer fb, FrameBuffer sampler,
                                    Matrix3x3Data vertMatData,
                                    VertexMatrixUpdater ptVmi,
                                    int updateFreq, String name) {
        super(rtt, pt, samp, fb, sampler, vertMatData, ptVmi, 1f, updateFreq, name);
        affineData = new FloatData(0f);
        scaleData = new FloatData(1f);
        pt.addUniform("u_Affine", affineData);
        pt.addUniform("u_Scale", scaleData);
    }
    private final FloatData affineData, scaleData;
}
