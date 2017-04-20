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

public class OperationGenerator {

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

    private ImageOperation generateFilter(OperationType type) {
        if(type.isColorblindType()) {
            return new ColorblindOperation(
                    colorMapShader,
                    vertexMatrixData,
                    eyeUpdate,
                    colorMapMatrixData,
                    type);
        }

        switch(type) {
            case ANAGLYPH:
                return new AnaglyphOperation(
                        colorMapShader,
                        vertexMatrixData,
                        eyeUpdate,
                        colorMapMatrixData,
                        anaglyphMaps[0],
                        anaglyphMaps[1]);
            case HUE_ROTATION:
                return new HueRotationOperation(
                        colorMapShader,
                        vertexMatrixData,
                        eyeUpdate,
                        colorMapMatrixData, 600);
        }

        Shader sh = type.generateShader(fromCameraShaderGenerator.copy());
        return new SingleShaderOperation(sh, vertexMatrixData, eyeUpdate, type.getName());
    }

    /*
     * This shader computes local contrast
     */
    private ImageOperation generateLocalContrastOperation(float fade) {
        return LocalContrastOperation.create(
                fromCameraShaderGenerator.copy(),
                fromTextureShaderGenerator.copy(),
                buffers[0],
                buffers[1],
                buffers[2],
                fade,
                vertexMatrixData,
                eyeUpdate);
    }

    /*
     * Toon filter
     */
    private ImageOperation generateToonOperation() {
        return ToonOperation.create(
                fromCameraShaderGenerator.copy(),
                fromTextureShaderGenerator.copy(),
                buffers[0],
                buffers[1],
                vertexMatrixData,
                eyeUpdate);
    }

    /*
     * This shader implements image integration and histogram equalization
     */
    private ImageOperation generateNightVisionOperation(int halfLife) {
        return NightVisionOperation.create(
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

    /*
     * A simple contrast adjustment filter
     */
    private ImageOperation generateLinearContrastOperation() {
        return LinearContrastOperation.create(
                fromCameraShaderGenerator.copy(),
                fromTextureShaderGenerator.copy(),
                buffers[0],
                sampleBuffer,
                vertexMatrixData,
                eyeUpdate,
                0);
    }

    /*
     * A histogram equalization filter that equalizes each channel independently
     */
    private ImageOperation generateAdvancedContrastOperation(float windowScale) {
        return AdvancedContrastOperation.create(
                fromCameraShaderGenerator.copy(),
                fromTextureShaderGenerator.copy(),
                buffers[0],
                sampleBuffer,
                vertexMatrixData,
                eyeUpdate,
                0,
                windowScale);
    }

    public Collection<ImageOperation> generateImageOperations() {
        ArrayList<ImageOperation> imageOperations = new ArrayList<>();
        for(OperationType ft: OperationType.values())
            imageOperations.add(generateFilter(ft));
        imageOperations.add(generateNightVisionOperation(60));
        imageOperations.add(generateLinearContrastOperation());
        imageOperations.add(generateAdvancedContrastOperation(1f));
        imageOperations.add(generateAdvancedContrastOperation(.25f));
        imageOperations.add(generateLocalContrastOperation(.9f));
        imageOperations.add(generateToonOperation());
        return imageOperations;
    }

    private static final String TAG = OperationGenerator.class.getName();

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

    public OperationGenerator(ResourceLoader rl) {

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

        threshData = new FloatData(THRESHOLD);
        strictData = new FloatData(STRICTNESS);
        defaultShaderInitializer.addUniform("u_Threshold", threshData);
        defaultShaderInitializer.addUniform("u_Strictness", strictData);

        GLTools.checkGLError(TAG, "initGL");

        ShaderGenerator tmp = fromCameraShaderGenerator.copy();
        tmp.setComputeColor(R.raw.color_map);
        colorMapShader = tmp.generateShader();
        colorMapShader.addUniform("u_ColorMapMatrix", colorMapMatrixData);

    }

    private static final float[][] anaglyphMaps = new float[][] {
            {1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f}
    };

    private final Shader colorMapShader;

    private final ShaderGenerator fromCameraShaderGenerator, fromTextureShaderGenerator;
    private final ShaderInitializer defaultShaderInitializer;

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
