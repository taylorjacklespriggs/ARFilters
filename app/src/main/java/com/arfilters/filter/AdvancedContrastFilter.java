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

import com.arfilters.GLTools;
import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;
import com.arfilters.shader.data.Vector2Data;
import com.arfilters.shader.data.Vector4Data;
import com.taylorjs.hproject.arfilters.R;

import java.nio.ByteBuffer;

import static com.arfilters.GLTools.FrameBuffer;

public class AdvancedContrastFilter extends ImageSampleFilter {

    public static AdvancedContrastFilter create(ShaderGenerator camGen,
                                                ShaderGenerator texGen,
                                                FrameBuffer fb,
                                                Matrix3x3Data vertMatData,
                                                VertexMatrixUpdater ptVmi,
                                                int updateFreq, float windowScale) {
        camGen.setComputeColor(R.raw.passthrough);
        Shader ctt = camGen.generateShader();
        texGen.setComputeColor(R.raw.advanced_contrast);
        Shader contrast = texGen.generateShader();
        int w = (int)(fb.getWidth() * windowScale);
        int h = (int)(fb.getHeight() * windowScale);
        int x = (fb.getWidth() - w) / 2;
        int y = (fb.getHeight() - h) / 2;
        return new AdvancedContrastFilter(ctt, contrast, fb, vertMatData,
                ptVmi, x, y, w, h, updateFreq, "Advanced Contrast");
    }

    private class ContrastInfo implements ImageSampler {
        @Override
        public void feed(int x, int y, int r, int g, int b, int a) {
            ++histogram[r][0];
            ++histogram[g][1];
            ++histogram[b][2];
        }
        @Override
        public void finish() {
            for(int c = 0; c < 3; ++c)
                histogram[0][c] = 0;
            for(int i = 0; i < 255; ++i)
                for(int c = 0; c < 3; ++c)
                    histogram[i+1][c] += histogram[i][c];
            cdf.position(0);
            for(int i = 0; i < 256; ++i) {
                for (int c = 0; c < 3; ++c)
                    cdf.put((byte) (histogram[i][c] * 255 / histogram[255][c]));
                cdf.put((byte) 0);
            }
            cdf.position(0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureLocation);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, 256, 1,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, cdf);
        }
    }

    @Override
    protected ImageSampler createImageSampler() {
        for(int i = 0; i < 256; ++i)
            for(int c = 0; c < 3; ++c)
                histogram[i][c] = 0;
        return new ContrastInfo();
    }

    private AdvancedContrastFilter(Shader rtt, Shader pt, FrameBuffer fb,
                                   Matrix3x3Data vertMatData,
                                   VertexMatrixUpdater ptVmi,
                                   int x, int y, int w, int h,
                                   int updateFreq, String name) {
        super(rtt, pt, fb, vertMatData, ptVmi, x, y, w, h, updateFreq, name);
        info = new ContrastInfo();
        histogram = new int[256][3];
        cdf = ByteBuffer.allocateDirect(4*256);
        for(int i = 0; i < 256; ++i) {
            for (int c = 0; c < 3; ++c) {
                histogram[i][c] = i;
                cdf.put((byte) i);
            }
            cdf.put((byte) 0);
        }
        cdf.position(0);
        textureLocation = GLTools.genTexture(GLES20.GL_TEXTURE_2D,
                GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256, 1, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, cdf);
        pt.addUniform("u_CDF", new TextureLocationData(GLES20.GL_TEXTURE_2D, 1,
                textureLocation));
        float width = fb.getWidth(), height = fb.getHeight();
        pt.addUniform("u_BLCorner", new Vector2Data(x/width,y/height));
        pt.addUniform("u_TRCorner", new Vector2Data((x+w)/width,(y+h)/height));
    }
    private final ContrastInfo info;
    private final int histogram[][];
    private final ByteBuffer cdf;
    private final int textureLocation;
}
