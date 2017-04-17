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

import static com.arfilters.GLTools.FrameBuffer;
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

    private static final int CAMERA_WIDTH = 1920, CAMERA_HEIGHT = 1080;
    private static final int BUFFER_WIDTH = CAMERA_WIDTH, BUFFER_HEIGHT = CAMERA_HEIGHT;
    private static final float THRESHOLD = .1f;
    private static final float STRICTNESS = 20f;
    private static final float SAMPLE_SCALE = 1f/8f;

    public int getCameraTextureLocation() {
        return cameraTextureLocation;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    private Filter generateFilter(FilterType type) {
        if(type.isColorblindType()) {
            return new ColorblindFilter(
                    colorMapShader,
                    vertexMatrixData,
                    eyeUpdate,
                    colorMapMatrixData,
                    type);
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

        fromCameraShaderGenerator.setInitializer(
                (type.getClassType() == FilterClass.EDGES)
                        ? edgeShaderInitializer
                        : defaultShaderInitializer);

        Shader sh = type.generateShader(fromCameraShaderGenerator.copy());
        return new SingleShaderFilter(sh, vertexMatrixData, eyeUpdate, type.getName());
    }

    /*
     * This shader computes local contrast
     */
    private Filter generateLocalContrastFilter() {
        return LocalContrastFilter.create(
                fromCameraShaderGenerator.copy(),
                fromTextureShaderGenerator.copy(),
                buffers[0],
                buffers[1],
                buffers[2],
                vertexMatrixData,
                eyeUpdate);
    }

    /*
     * Toon filter
     */
    private Filter generateToonFilter() {
        return ToonFilter.create(
                fromCameraShaderGenerator.copy(),
                fromTextureShaderGenerator.copy(),
                buffers[0],
                buffers[1],
                threshData,
                vertexMatrixData,
                eyeUpdate);
    }

    /*
     * This shader fades with a 16bit monochrome texture
     */
    private Filter generateMonochromeFadingFilter(float halfLife) {
        return FadingFilter.create(
                fromCameraShaderGenerator.copy(),
                fromTextureShaderGenerator.copy(),
                halfLife,
                buffers[0],
                buffers[1],
                buffers[2],
                sampleBuffer,
                vertexMatrixData,
                eyeUpdate);
    }

    private Filter generateRTTFilter() {
        return RTTFilter.create(
                fromCameraShaderGenerator,
                fromTextureShaderGenerator,
                buffers[0],
                vertexMatrixData,
                eyeUpdate);
    }

    private Filter generateContrastFilter() {
        return LinearContrastFilter.create(
                fromCameraShaderGenerator,
                fromTextureShaderGenerator,
                buffers[0],
                sampleBuffer,
                vertexMatrixData,
                eyeUpdate,
                0);
    }

    private Filter generateTestSampleFilter() {
        return TestSampleFilter.create(
                fromCameraShaderGenerator,
                fromTextureShaderGenerator,
                buffers[0],
                sampleBuffer,
                vertexMatrixData,
                eyeUpdate);
    }

    private Filter generateAdvancedContrastFilter(float windowScale) {
        return AdvancedContrastFilter.create(
                fromCameraShaderGenerator,
                fromTextureShaderGenerator,
                buffers[0],
                sampleBuffer,
                vertexMatrixData,
                eyeUpdate,
                0,
                windowScale);
    }

    public Collection<Filter> generateFilters() {
        ArrayList<Filter> filters = new ArrayList<>();
        filters.add(generateTestSampleFilter());
        filters.add(generateLocalContrastFilter());
        filters.add(generateToonFilter());
        filters.add(generateRTTFilter());
        filters.add(generateAdvancedContrastFilter(1f));
        filters.add(generateAdvancedContrastFilter(.5f));
        filters.add(generateAdvancedContrastFilter(.25f));
        filters.add(generateContrastFilter());
        filters.add(generateMonochromeFadingFilter(20f/60f));
        for(FilterType ft: FilterType.values())
            filters.add(generateFilter(ft));
        return filters;
    }

    private static final String TAG = FilterGenerator.class.getName();

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
                float Cw = scale*CAMERA_WIDTH;
                float Ch = scale*CAMERA_HEIGHT;
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

        buffers = new FrameBuffer[3];
        for(int i = 0; i < buffers.length; ++i)
            buffers[i] = new GLTools.FrameBuffer(BUFFER_WIDTH, BUFFER_HEIGHT, GLES20.GL_RGBA, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);

        sampleBuffer = new GLTools.FrameBuffer((int)(BUFFER_WIDTH*SAMPLE_SCALE),
                (int)(BUFFER_HEIGHT*SAMPLE_SCALE), GLES20.GL_RGBA, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);

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
        fromCameraShaderGenerator.setInitializer(defaultShaderInitializer);
        fromTextureShaderGenerator.setInitializer(defaultShaderInitializer);

        edgeShaderInitializer = genShaderInit();

        threshData = new FloatData(THRESHOLD);
        strictData = new FloatData(STRICTNESS);
        edgeShaderInitializer.addUniform("u_Threshold", threshData);
        edgeShaderInitializer.addUniform("u_Strictness", strictData);

        GLTools.checkGLError(TAG, "initGL");

        ShaderGenerator tmp = fromCameraShaderGenerator.copy();
        tmp.setComputeColor(R.raw.color_map);
        colorMapShader = tmp.generateShader();
        colorMapShader.addUniform("u_ColorMapMatrix", colorMapMatrixData);

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

    private final Shader colorMapShader;

    private final ShaderGenerator fromCameraShaderGenerator, fromTextureShaderGenerator;
    private final ShaderInitializer defaultShaderInitializer, edgeShaderInitializer;

    private final FrameBuffer[] buffers;
    private final FrameBuffer sampleBuffer;

    private final ViewInfo viewInfo = new ViewInfo();
    private final VertexMatrixUpdater eyeUpdate;

    private final Matrix3x3Data vertexMatrixData, colorMapMatrixData;

    private final VertexAttributeData faceVertexData = new VertexAttributeData(
            VertexData.FACE_COORD_DIMENSION, VertexData.FACE_NUMBER_VERTICES);

    private final VertexAttributeData faceTexCoordData =
            new VertexAttributeData(VertexData.FACE_TEX_COORD_DIMENSION,
                    VertexData.FACE_NUMBER_VERTICES);

    private final FloatData strictData, threshData;

    private TextureLocationData cameraLocationData;
    private int cameraTextureLocation;

}
