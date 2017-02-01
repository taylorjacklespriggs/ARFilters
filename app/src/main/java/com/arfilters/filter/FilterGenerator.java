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

package com.arfilters.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.arfilters.GLTools;
import com.arfilters.ResourceLoader;
import com.arfilters.VertexData;
import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.arfilters.shader.ShaderInitializer;
import com.arfilters.shader.ViewInfo;
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;
import com.arfilters.shader.data.VertexAttributeData;
import com.taylorjs.hproject.arfilters.R;

import java.util.ArrayList;
import java.util.Collection;

import javax.microedition.khronos.opengles.GL10;

public class FilterGenerator {

    public static final int WIDTH = 1920, HEIGHT = 1080;

    public int getCameraTextureLocation() {
        return cameraTextureLocation;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    private ShaderInitializer getInitializer(FilterClass cls) {
        switch(cls) {
            case PLAIN:
            case TEXTURE_WARP:
                return defaultShaderInitializer;
            case COLOR_MAP:
                return colorMapShaderInitializer;
            case EDGES:
                return edgeShaderInitializer;
        }
        return defaultShaderInitializer;
    }

    private Filter generateFilter(FilterType type) {
        if(type.isColorblindType()) {
            return new ColorblindFilter(
                    colorMapShader,
                    vertexMatrixData,
                    eyeUpdate,
                    colorMapMatrixData,
                    colorblindMaps[type.getColorblindIndex()]);
        }

        switch(type) {
            case ANAGLYPH:
                return new AnaglyphFilter(
                        colorMapShader,
                        vertexMatrixData,
                        eyeUpdate,
                        colorMapMatrixData,
                        anaglyphMaps[0],
                        anaglyphMaps[1]);
            case HUE_ROTATION:
                return new HueRotationFilter(
                        colorMapShader,
                        vertexMatrixData,
                        eyeUpdate,
                        colorMapMatrixData, 600);
        }

        fromCameraShaderGenerator.setInitializer((type.getClassType() == FilterClass.EDGES) ? edgeShaderInitializer : defaultShaderInitializer);

        Shader sh = type.generateShader(fromCameraShaderGenerator);
        return new SingleShaderFilter(sh, vertexMatrixData, eyeUpdate);
    }

    /*
     * This shader mixes the backbuffer with the camera texture for a fading view
     */
    private Filter generateFadingFilter() {
        TextureLocationData
                camLoc = new TextureLocationData(GLES20.GL_TEXTURE_2D, 0, cameraBuffer.getTextureID()),
                bbLoc = new TextureLocationData(GLES20.GL_TEXTURE_2D, 1, backBuffer.getTextureID()),
                fbLoc = new TextureLocationData(GLES20.GL_TEXTURE_2D, 0, frontBuffer.getTextureID());

        // generate camera to texture shader
        fromCameraShaderGenerator.setInitializer(defaultShaderInitializer);
        Shader cttShader = fromCameraShaderGenerator.generateDefaultShader();

        // generate mixing shader
        FloatData firstAmount = new FloatData(.8f),
                secondAmount = new FloatData(.9f);
        ShaderInitializer si = genShaderInit(camLoc);
        si.addUniform("u_FirstAmount", firstAmount);
        si.addUniform("u_SecondAmount", secondAmount);
        si.addUniform("u_AlternateTexture", bbLoc);
        fromTextureShaderGenerator.setInitializer(si);
        Shader mixShader = fromTextureShaderGenerator.generateModifiedColorShader(R.raw.mixing_texture, false);

        // create the passThrough shader
        si = genShaderInit(fbLoc);
        fromTextureShaderGenerator.setInitializer(si);
        Shader ptShader = fromTextureShaderGenerator.generateDefaultShader();

        return new FadingFilter(cttShader, mixShader, ptShader, frontBuffer,
                backBuffer, cameraBuffer, vertexMatrixData, fbLoc, bbLoc,
                eyeUpdate);
    }

    private Filter generateRTTFilter() {
        // generate camera to texture shader
        fromCameraShaderGenerator.setInitializer(defaultShaderInitializer);
        Shader rttShader = fromCameraShaderGenerator.generateDefaultShader();

        // generate passthrough shader
        ShaderInitializer si = genShaderInit(new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 0, frontBuffer.getTextureID()));
        fromCameraShaderGenerator.setInitializer(si);
        Shader ptShader = fromCameraShaderGenerator.generateModifiedTextureFragmentShader(R.raw.default_texture, false);

        return new RTTFilter(rttShader, ptShader, frontBuffer, vertexMatrixData, eyeUpdate);
    }

    public Collection<Filter> generateFilters() {
        ArrayList<Filter> filters = new ArrayList<>();
        filters.add(generateFadingFilter());
        filters.add(generateRTTFilter());
        for(FilterType ft: FilterType.values()) {
            filters.add(generateFilter(ft));
        }
        return filters;
    }

    private static final String TAG = "FilterGenerator";

    private ShaderInitializer genShaderInit(TextureLocationData texData) {
        ShaderInitializer si = new ShaderInitializer("a_Position",
                faceVertexData, "a_TexCoord", faceTexCoordData,
                VertexData.FACE_NUMBER_VERTICES);
        si.addUniform("u_VertexTransform", vertexMatrixData);
        si.addUniform("u_Texture", texData);
        return si;
    }

    private ShaderInitializer genShaderInit() {
        return genShaderInit(cameraLocationData);
    }

    public FilterGenerator(ResourceLoader rl) {

        eyeUpdate = new VertexMatrixUpdater() {
            @Override
            public float[] updateVertexMatrix(ViewInfo vi) {
                float scale = .6f;
                float Cw = scale*WIDTH;
                float Ch = scale*HEIGHT;
                float Vw = vi.getWidth();
                float Vh = vi.getHeight();

                return new float[]{
                        Cw / Vw, 0, 0,
                        0, -Ch / Vh, 0,
                        (1f - Cw / Vw) / 2f, (1f - Ch / Vh) / 2f, 1
                };
            }
        };

        int cameraVertexShader = GLTools.loadGLShader(TAG,
                GLES20.GL_VERTEX_SHADER,
                rl.readRawTextFile(R.raw.vertex));
        fromCameraShaderGenerator = new ShaderGenerator(rl, cameraVertexShader,
                rl.readRawTextFile(R.raw.camera_texture),
                rl.readRawTextFile(R.raw.default_texture_coordinates),
                rl.readRawTextFile(R.raw.passthrough),
                rl.readRawTextFile(R.raw.fs_main), true, false,
                com.arfilters.shader.Precision.MEDIUM);
        fromTextureShaderGenerator = new ShaderGenerator(rl, cameraVertexShader,
                rl.readRawTextFile(R.raw.default_texture),
                rl.readRawTextFile(R.raw.default_texture_coordinates),
                rl.readRawTextFile(R.raw.passthrough),
                rl.readRawTextFile(R.raw.fs_main), false, false,
                com.arfilters.shader.Precision.MEDIUM);

        frontBuffer = new GLTools.FrameBuffer(WIDTH, HEIGHT, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
        backBuffer = new GLTools.FrameBuffer(WIDTH, HEIGHT, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
        cameraBuffer = new GLTools.FrameBuffer(WIDTH, HEIGHT, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);

        vertexMatrixData = new Matrix3x3Data();
        colorMapMatrixData = new Matrix3x3Data();

        GLTools.checkGLError(TAG, "gen framebuffer");

        // Create texture for camera preview
        cameraTextureLocation = GLTools.genTexture(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_LINEAR,
                GL10.GL_CLAMP_TO_EDGE);

        cameraLocationData = new TextureLocationData(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0, cameraTextureLocation);

        faceVertexData.updateData(VertexData.FACE_COORDS);

        faceTexCoordData.updateData(VertexData.FACE_TEX_COORDS);

        defaultShaderInitializer = genShaderInit();

        edgeShaderInitializer = genShaderInit();
        edgeShaderInitializer.addUniform("u_Threshold", threshData);
        edgeShaderInitializer.addUniform("u_Strictness", strictData);

        colorMapShaderInitializer = genShaderInit();
        colorMapShaderInitializer.addUniform("u_ColorMapMatrix", colorMapMatrixData);

        GLTools.checkGLError(TAG, "initGL");

        fromCameraShaderGenerator.setInitializer(colorMapShaderInitializer);
        colorMapShader = fromCameraShaderGenerator
                .generateModifiedColorShader(R.raw.color_map, false);
    }

    private static final float[][] colorblindMaps = new float[][] {
            {       // protanopia
                    0.567f, 0.433f, 0.0f,
                    0.558f, 0.442f, 0.0f,
                    0.0f,   0.242f, 0.758f
            }, {    // protanomaly
                    0.817f, 0.183f, 0.0f,
                    0.333f, 0.667f, 0.0f,
                    0.0f,   0.125f, 0.875f
            }, {    // deuteranopia
                    0.625f, 0.375f, 0.0f,
                    0.7f,   0.3f,   0.0f,
                    0.0f,   0.3f,   0.7f
            }, {    // deuteranomaly
                    0.8f,   0.2f,   0.0f,
                    0.258f, 0.742f, 0.0f,
                    0.0f,   0.142f, 0.858f
            }, {    // tritanopia
                    0.95f,  0.05f,  0.0f,
                    0.0f,   0.433f, 0.567f,
                    0.0f,   0.475f, 0.525f
            }, {    // tritanomaly
                    0.967f, 0.033f, 0.0f,
                    0.0f,   0.733f, 0.267f,
                    0.0f,   0.183f, 0.817f
            }, {    // achromatopsia
                    0.299f, 0.587f, 0.114f,
                    0.299f, 0.587f, 0.114f,
                    0.299f, 0.587f, 0.114f
            }, {    // achromatomaly
                    0.618f, 0.320f, 0.062f,
                    0.163f, 0.775f, 0.062f,
                    0.163f, 0.320f, 0.516f
            }
    };

    private static final float[][] anaglyphMaps = new float[][] {
            {1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f}
    };

    private static final float[] identity = new float[] {
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
    };

    private final Shader colorMapShader;

    private final ShaderGenerator fromCameraShaderGenerator, fromTextureShaderGenerator;
    private final ShaderInitializer defaultShaderInitializer, edgeShaderInitializer, colorMapShaderInitializer;

    private final GLTools.FrameBuffer frontBuffer, backBuffer, cameraBuffer;

    private final ViewInfo viewInfo = new ViewInfo();
    private final VertexMatrixUpdater eyeUpdate;

    private final Matrix3x3Data vertexMatrixData, colorMapMatrixData;

    private final FloatData threshData = new FloatData(.3f);
    private final FloatData strictData = new FloatData(20f);

    private final VertexAttributeData faceVertexData = new VertexAttributeData(
            VertexData.FACE_COORD_DIMENSION, VertexData.FACE_NUMBER_VERTICES);

    private final VertexAttributeData faceTexCoordData =
            new VertexAttributeData(VertexData.FACE_TEX_COORD_DIMENSION,
                    VertexData.FACE_NUMBER_VERTICES);

    private TextureLocationData cameraLocationData;
    private int cameraTextureLocation;

}
