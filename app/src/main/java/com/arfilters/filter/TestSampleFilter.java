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

import android.opengl.GLES20;

import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;
import com.taylorjs.hproject.arfilters.R;

import static com.arfilters.GLTools.FrameBuffer;

public class TestSampleFilter extends BufferedFilter {

    private static final String TAG = TestSampleFilter.class.getName();

    public static TestSampleFilter create(ShaderGenerator camGen,
                                          ShaderGenerator texGen,
                                          FrameBuffer fb,
                                          FrameBuffer sampler,
                                          Matrix3x3Data vertMatData,
                                          VertexMatrixUpdater ptVmi) {
        camGen.setComputeColor(R.raw.passthrough);
        Shader ctt = camGen.generateShader();
        texGen.setComputeColor(R.raw.test_passthrough);
        Shader samp = texGen.generateShader();
        texGen.setComputeColor(R.raw.passthrough);
        Shader passthrough = texGen.generateShader();
        return new TestSampleFilter(ctt, passthrough, samp, fb, sampler,
                vertMatData, ptVmi);
    }

    @Override
    protected void renderToBuffers() {
        frameBuffer.enable();
        rttShader.draw();
        sampleBuffer.enable();
        sampleShader.draw();
    }

    private TestSampleFilter(Shader rtt, Shader pt, Shader samp, FrameBuffer fb, FrameBuffer sampler,
                             Matrix3x3Data vertMatData,
                             VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi, "Test Sampler");
        sampleBuffer = sampler;
        sampleShader = samp;
        rttShader = rtt;
        frameBuffer = fb;
        pt.addUniform("u_Texture", new TextureLocationData(GLES20.GL_TEXTURE_2D, 0, sampleBuffer.getTextureID()));
        sampleShader.addUniform("u_Texture", new TextureLocationData(GLES20.GL_TEXTURE_2D, 0, frameBuffer.getTextureID()));
    }

    private Shader rttShader, sampleShader;
    private FrameBuffer frameBuffer, sampleBuffer;
}
