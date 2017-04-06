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

package com.arfilters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.arfilters.filter.Filter;
import com.arfilters.filter.FilterGenerator;
import com.arfilters.shader.ViewInfo;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.taylorjs.hproject.arfilters.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;

import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.util.Pair;
import android.widget.Toast;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    private static final String TAG = "MainActivity";

    private int cameraTextureLocation;

    private ViewInfo viewInfo;

    private Camera hardwareCamera;
    private SurfaceTexture cameraSurfaceTexture;

    private Vibrator vibrator;
    private Collection<Filter> filters;
    private Iterator<Filter> filterIterator;
    private Filter currentFilter;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

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

        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
    }

    @Override
    public void onRendererShutdown() {

        for(Filter f: filters)
            f.cleanup();

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
        cameraSurfaceTexture = new SurfaceTexture(cameraTextureLocation);
        try {
            hardwareCamera.setPreviewTexture(cameraSurfaceTexture);
            hardwareCamera.startPreview();
        } catch (IOException ioe) {
            Log.e(TAG, "Some problem while starting camera preview: "+ioe);
            ioe.printStackTrace();
            Toast.makeText(this, "Could not open camera!", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        if(hardwareCamera != null) {
            Log.i(TAG, "onSurfaceCreated");
            initGL();
        }
    }

    private void initGL() {

        GLES20.glClearColor(0f, 0f, 0f, 1f); // Dark background so text shows up well.

        ResourceLoader resourceLoader = new ResourceLoader(this);

        FilterGenerator filterGenerator = new FilterGenerator(resourceLoader);

        viewInfo = filterGenerator.getViewInfo();

        // Create texture for camera preview
        cameraTextureLocation = filterGenerator.getCameraTextureLocation();

        startCameraPreview();

        filters = filterGenerator.generateFilters();

        nextFilter();

        GLTools.checkGLError(TAG, "initGL");
    }

    private void applyFilter(Filter f) {
        currentFilter = f;
        Log.i(TAG, "switched to "+f.getName()+" filter");
        currentFilter.initialize();
    }

    private void nextFilter() {
        if(filterIterator == null || !filterIterator.hasNext()) {
            filterIterator = filters.iterator();
        }
        applyFilter(filterIterator.next());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCamera();
                }
            }
        }
    }

    int frameCount = 0;
    long time = System.nanoTime();
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        if(cameraSurfaceTexture != null) {
            long tmp = System.nanoTime();
            if(tmp - time > 1000000000L) {
                Log.i(TAG, frameCount+"fps");
                time = tmp;
                frameCount = 0;
            }

            // Update the camera preview texture
            cameraSurfaceTexture.updateTexImage();

            currentFilter.prepareView();

            GLTools.checkGLError(TAG, "onReadyToDraw");
            ++frameCount;
        }
    }

    @Override
    public void onDrawEye(Eye eye) {
        viewInfo.setEye(eye);
        if(hardwareCamera != null) {
            if(cameraSurfaceTexture == null) {
                initGL();
            }

            currentFilter.drawEye(viewInfo);
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    @Override
    public void onCardboardTrigger() {
        nextFilter();
        vibrator.vibrate(50);
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
