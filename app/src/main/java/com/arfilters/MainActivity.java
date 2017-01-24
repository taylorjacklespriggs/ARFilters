/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 * Modifications copyright (C) 2016 Taylor Jackle Spriggs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arfilters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.arfilters.shader.Shader;
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x2Data;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;
import com.arfilters.shader.data.VertexAttributeData;
import com.arfilters.shader.data.VertexData;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.taylorjs.hproject.arfilters.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;

import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.util.Pair;
import android.widget.Toast;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    private static final String TAG = "MainActivity";

    private enum Precision {
        LOW,
        MEDIUM,
        HIGH;

        public String getString(String primitive) {
            return "precision" + getPrefix() + primitive + ";\n";
        }

        private String getPrefix() {
            switch(this) {
                case LOW:
                    return " lowp ";
                case MEDIUM:
                    return " mediump ";
                case HIGH:
                    return " highp ";
                default:
                    return "";
            }
        }
    };

    private TextureLocationData cameraLocationData;
    private int cameraTextureLocation;

    private final Matrix3x3Data textureTransformData = new Matrix3x3Data();

    private final Matrix3x3Data colorMapData = new Matrix3x3Data();

    private final FloatData threshData = new FloatData(.3f);
    private final FloatData strictData = new FloatData(20f);

    private final VertexAttributeData faceVertexData = new VertexAttributeData(
            VertexData.FACE_COORD_DIMENSION, VertexData.FACE_NUMBER_VERTICES);

    private final VertexAttributeData faceTexCoordData = new VertexAttributeData(
            VertexData.FACE_TEX_COORD_DIMENSION, VertexData.FACE_NUMBER_VERTICES);

    private static final float[][] anaglyphMaps = new float[][] {
            {1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f}
    };

    private static final float[][] colorblindMaps = new float[][] {
            {1.0f,      0.0f,   0.0f,   0.0f,   1.0f,   0.0f,   0.0f,   0.0f,   1.0f}, // normal
            {0.567f,    0.433f, 0.0f,   0.558f, 0.442f, 0.0f,   0.0f,   0.242f, 0.758f}, // protanopia
            {0.817f,    0.183f, 0.0f,   0.333f, 0.667f, 0.0f,   0.0f,   0.125f, 0.875f}, // protanomaly
            {0.625f,    0.375f, 0.0f,   0.7f,   0.3f,   0.0f,   0.0f,   0.3f,   0.7f}, // deuteranopia
            {0.8f,      0.2f,   0.0f,   0.258f, 0.742f, 0.0f,   0.0f,   0.142f, 0.858f}, // deuteranomaly
            {0.95f,     0.05f,  0.0f,   0.0f,   0.433f, 0.567f, 0.0f,   0.475f, 0.525f}, // tritanopia
            {0.967f,    0.033f, 0.0f,   0.0f,   0.733f, 0.267f, 0.0f,   0.183f, 0.817f}, // tritanomaly
            {0.299f,    0.587f, 0.114f, 0.299f, 0.587f, 0.114f, 0.299f, 0.587f, 0.114f}, // achromatopsia
            {0.618f,    0.320f, 0.062f, 0.163f, 0.775f, 0.062f, 0.163f, 0.320f, 0.516f}  // achromatomaly
    };

    private Camera hardwareCamera;
    private SurfaceTexture cameraSurfaceTexture;

    private int vertexShader;
    private Shader directShader, anaglyphShader, invertedShader, chromaticEdgeShader,
                    enhancedEdgeShader, gradientEdgeShader, toonShader, colorblindShader,
                    zoomShader;
    private ArrayList<Shader> shaderList;
    private int shaderIndex, colorblindIndex;

    private Vibrator vibrator;

    private void setupCamera() {
        // Open camera
        Pair<Camera.CameraInfo, Integer> backCamera = getBackCamera();
        final int backCameraId = backCamera.second;
        hardwareCamera = Camera.open(backCameraId);
        int hCenter, wCenter, radius;
        hCenter = 0;
        wCenter = 0;
        radius = 100;
        Rect focus = new Rect(wCenter-radius, hCenter-radius,
                wCenter+radius, hCenter+radius);
        Camera.Parameters parameters = hardwareCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        ArrayList<Camera.Area> areas = new ArrayList<>();
        areas.add(new Camera.Area(focus, 1000));
        parameters.setFocusAreas(areas);
        hardwareCamera.setParameters(parameters);
    }

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Camera access is required.", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        101);
            }

        } else {
            setupCamera();
        }

        initializeGvrView();

    }

    public void initializeGvrView() {
        setContentView(R.layout.common_ui);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
    }

    @Override
    public void onRendererShutdown() {

        cameraSurfaceTexture.release();
        GLES20.glDeleteTextures(1, new int[]{cameraTextureLocation}, 0);

        if (hardwareCamera != null) {
            hardwareCamera.stopPreview();
            hardwareCamera.release();
        }

        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    private void startCameraPreview() {
        try {
            hardwareCamera.setPreviewTexture(cameraSurfaceTexture);
            hardwareCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        if(hardwareCamera != null) {
            Log.i(TAG, "onSurfaceCreated");
            initGL();
        }
    }

    private FloatBuffer createFloatBuffer(int count) {
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(count * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        return bbVertices.asFloatBuffer();
    }

    private FloatBuffer createFloatBuffer(float[] vals) {
        FloatBuffer fb = createFloatBuffer(vals.length);
        fb.put(vals);
        fb.position(0);
        return fb;
    }

    private void initGL() {

        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        // Create texture for camera preview
        cameraTextureLocation = GLTools.genTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        cameraSurfaceTexture = new SurfaceTexture(cameraTextureLocation);
        startCameraPreview();

        cameraLocationData = new TextureLocationData(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0,
                cameraTextureLocation);

        faceVertexData.updateData(VertexData.FACE_COORDS);

        faceTexCoordData.updateData(VertexData.FACE_TEX_COORDS);

        vertexShader = GLTools.loadGLShader(TAG, GLES20.GL_VERTEX_SHADER, readRawTextFile(R.raw.vertex));

        directShader = setupShader(R.raw.camera_texture, R.raw.default_texture_coordinates,
                R.raw.passthrough, true, false, Precision.MEDIUM);

        anaglyphShader = setupShader(R.raw.camera_texture, R.raw.default_texture_coordinates,
                R.raw.color_map, true, false, Precision.MEDIUM);
        anaglyphShader.addUniform("u_ColorMapMatrix", colorMapData);

        invertedShader = setupShader(R.raw.camera_texture, R.raw.default_texture_coordinates,
                R.raw.inverted, true, false, Precision.MEDIUM);

        String threshName = "u_Threshold";

        String strictName = "u_Strictness";

        chromaticEdgeShader = setupShader(R.raw.camera_texture, R.raw.default_texture_coordinates,
                R.raw.chromatic_edges, true, true, Precision.MEDIUM);
        chromaticEdgeShader.addUniform(threshName, threshData);
        chromaticEdgeShader.addUniform(strictName, strictData);

        enhancedEdgeShader = setupShader(R.raw.camera_texture, R.raw.default_texture_coordinates,
                R.raw.enhanced_edges, true, true, Precision.MEDIUM);
        enhancedEdgeShader.addUniform(threshName, threshData);
        enhancedEdgeShader.addUniform(strictName, strictData);

        gradientEdgeShader = setupShader(R.raw.camera_texture, R.raw.default_texture_coordinates,
                R.raw.gradient_edges, true, true, Precision.MEDIUM);
        gradientEdgeShader.addUniform(threshName, threshData);
        gradientEdgeShader.addUniform(strictName, strictData);

        toonShader = setupShader(R.raw.camera_texture, R.raw.default_texture_coordinates,
                R.raw.toon, true, true, Precision.MEDIUM);
        toonShader.addUniform(threshName, threshData);
        toonShader.addUniform(strictName, strictData);

        colorblindShader = setupShader(R.raw.camera_texture, R.raw.default_texture_coordinates,
                R.raw.color_map, true, false, Precision.MEDIUM);
        colorblindShader.addUniform("u_ColorMapMatrix", colorMapData);

        zoomShader = setupShader(R.raw.camera_texture, R.raw.zoomed_texture_coordinates,
                R.raw.passthrough, true, false, Precision.HIGH);

        shaderList = new ArrayList<>();
        shaderList.add(zoomShader);
        shaderList.add(colorblindShader);
        shaderList.add(directShader);
        shaderList.add(gradientEdgeShader);
        shaderList.add(enhancedEdgeShader);
        shaderList.add(invertedShader);
        shaderList.add(anaglyphShader);
        shaderList.add(toonShader);
        shaderList.add(chromaticEdgeShader);

        shaderIndex = 0;

        GLTools.checkGLError(TAG, "initGL");
    }

    private Shader setupShader(int getTextureFragmentID, int getTextureCoordinatesID,
                               int computeColorID, boolean useCamera, boolean useDerivatives,
                               Precision floatPrecision) {
        StringBuilder sb = new StringBuilder();
        if(useCamera) {
            sb.append("#extension GL_OES_EGL_image_external : require\n");
        }
        if(useDerivatives) {
            sb.append("#extension GL_OES_standard_derivatives : enable\n");
        }
        sb.append(floatPrecision.getString("float"));
        sb.append(readRawTextFile(getTextureFragmentID));
        sb.append(readRawTextFile(getTextureCoordinatesID));
        sb.append(readRawTextFile(computeColorID));
        sb.append(readRawTextFile(R.raw.fs_main));
        int fs = GLTools.loadGLShader(TAG, GLES20.GL_FRAGMENT_SHADER, sb.toString());

        Shader shader = new Shader(vertexShader, fs);

        shader.createVertices("a_Position", faceVertexData, "a_TexCoord", faceTexCoordData,
                VertexData.FACE_NUMBER_VERTICES);

        shader.addUniform("u_Texture", cameraLocationData);

        shader.addUniform("u_TexCoordTransform", textureTransformData);

        return shader;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCamera();
                }
            }
        }
    }

    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {

        if(cameraSurfaceTexture != null) {
            // Update the camera preview texture
            cameraSurfaceTexture.updateTexImage();

            GLTools.checkGLError(TAG, "onReadyToDraw");
        }
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        if(hardwareCamera != null) {
            if(cameraSurfaceTexture == null) {
                initGL();
            }

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            drawFace(eye);
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}


    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");

        nextShader();

        // Always give user feedback.
        vibrator.vibrate(50);
    }

    private Shader getCurrentShader() {
        return shaderList.get(shaderIndex);
    }

    private boolean nextColourblindShader() {
        ++colorblindIndex;
        if(colorblindIndex < 9) {
            colorMapData.updateData(colorblindMaps[colorblindIndex]);
            Log.i(TAG, "Currently selected shader is colourblind shader index "+ colorblindIndex);
            return true;
        }
        colorblindIndex = 0;
        return false;
    }

    private void nextShader() {
        if(getCurrentShader() == colorblindShader) {
            if(nextColourblindShader())
                return;
        }
        ++shaderIndex;
        shaderIndex %= shaderList.size();
        if(getCurrentShader() == colorblindShader)
            nextColourblindShader();
    }

    /**
     * Draw the cube.
     *
     * <p>We've set all of our transformation matrices. Now we simply pass them into the shader.
     */
    public void drawFace(Eye eye) {
        float scale = .6f;
        float Cw = scale*1920;
        float Ch = scale*1080;
        float Vw = eye.getViewport().width;
        float Vh = eye.getViewport().height;

        float[] texTransformMat = new float[] {
                Vw/Cw,          0,              0,
                0,              Vh/Ch,          0,
                (1f-Vw/Cw)/2f,  (1f-Vh/Ch)/2f,  1
        };
        textureTransformData.updateData(texTransformMat);

        if(getCurrentShader() == anaglyphShader) {
            colorMapData.updateData(anaglyphMaps[(eye.getType()==Eye.Type.RIGHT) ? 1 : 0]);
        }

        getCurrentShader().draw();

        GLTools.checkGLError(TAG, "Drawing face");
    }

    private Pair<Camera.CameraInfo, Integer> getBackCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        final int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return new Pair<>(cameraInfo, i);
            }
        }
        return null;
    }
}
