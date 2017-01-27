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
import android.util.Log;

import com.arfilters.GLTools;
import com.arfilters.ResourceLoader;
import com.arfilters.VertexData;
import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.arfilters.shader.ViewInfo;
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;
import com.arfilters.shader.data.VertexAttributeData;
import com.taylorjs.hproject.arfilters.R;

import java.util.ArrayList;
import java.util.Collection;

public class FilterGenerator {

    public int getCameraTextureLocation() {
        return cameraTextureLocation;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    private void prepareShader(Shader sh) {
        sh.createVertices("a_Position", faceVertexData, "a_TexCoord", faceTexCoordData,
                VertexData.FACE_NUMBER_VERTICES);
        sh.addUniform("u_Texture", cameraLocationData);
        viewInfo.prepareShaderTextureTransformationMatrix(sh, "u_TexCoordTransform");
    }

    public Filter generateFilter(FilterType type) {
        if(type.isColorblindType()) {
            return new ColorblindFilter(colorMapFilter, colorblindMaps[type.getColorblindIndex()]);
        }

        switch(type) {
            case ANAGLYPH:
                return new AnaglyphFilter(colorMapFilter, anaglyphMaps[0], anaglyphMaps[1]);
            case HUE_ROTATION:
                return new HueRotationFilter(colorMapFilter, 600);
        }

        Shader sh = type.generateShader(initialPassShaderGenerator);
        prepareShader(sh);
        if(type.getClassType() == FilterClass.EDGES) {
            sh.addUniform("u_Threshold", threshData);
            sh.addUniform("u_Strictness", strictData);
        }
        return new SingleShaderFilter(sh);
    }

    public Collection<Filter> generateFilters() {
        ArrayList<Filter> filters = new ArrayList<>();
        for(FilterType ft: FilterType.values()) {
            filters.add(generateFilter(ft));
        }
        return filters;
    }

    private static final String TAG = "FilterGenerator";

    public FilterGenerator(ResourceLoader rl) {
        resourceLoader = rl;

        cameraVertexShader = GLTools.loadGLShader(TAG, GLES20.GL_VERTEX_SHADER,
                resourceLoader.readRawTextFile(R.raw.vertex));
        initialPassShaderGenerator = new ShaderGenerator(resourceLoader, cameraVertexShader,
                        resourceLoader.readRawTextFile(R.raw.camera_texture),
                        resourceLoader.readRawTextFile(R.raw.default_texture_coordinates),
                        resourceLoader.readRawTextFile(R.raw.passthrough),
                        resourceLoader.readRawTextFile(R.raw.fs_main), true, false, com.arfilters.shader.Precision.MEDIUM);

        // Create texture for camera preview
        cameraTextureLocation = GLTools.genTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

        cameraLocationData = new TextureLocationData(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0,
                cameraTextureLocation);

        faceVertexData.updateData(VertexData.FACE_COORDS);

        faceTexCoordData.updateData(VertexData.FACE_TEX_COORDS);

        GLTools.checkGLError(TAG, "initGL");

        Shader colShader = initialPassShaderGenerator.generateModifiedColorShader(R.raw.color_map,
                false);
        prepareShader(colShader);

        colorMapFilter = new ColorMapFilter(colShader);
    }

    private final ResourceLoader resourceLoader;
    private final ShaderGenerator initialPassShaderGenerator;

    private TextureLocationData cameraLocationData;
    private int cameraTextureLocation;

    private ColorMapFilter colorMapFilter;

    private final ViewInfo viewInfo = new ViewInfo();

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
            {0.567f,    0.433f, 0.0f,   0.558f, 0.442f, 0.0f,   0.0f,   0.242f, 0.758f},    // protanopia
            {0.817f,    0.183f, 0.0f,   0.333f, 0.667f, 0.0f,   0.0f,   0.125f, 0.875f},    // protanomaly
            {0.625f,    0.375f, 0.0f,   0.7f,   0.3f,   0.0f,   0.0f,   0.3f,   0.7f},      // deuteranopia
            {0.8f,      0.2f,   0.0f,   0.258f, 0.742f, 0.0f,   0.0f,   0.142f, 0.858f},    // deuteranomaly
            {0.95f,     0.05f,  0.0f,   0.0f,   0.433f, 0.567f, 0.0f,   0.475f, 0.525f},    // tritanopia
            {0.967f,    0.033f, 0.0f,   0.0f,   0.733f, 0.267f, 0.0f,   0.183f, 0.817f},    // tritanomaly
            {0.299f,    0.587f, 0.114f, 0.299f, 0.587f, 0.114f, 0.299f, 0.587f, 0.114f},    // achromatopsia
            {0.618f,    0.320f, 0.062f, 0.163f, 0.775f, 0.062f, 0.163f, 0.320f, 0.516f}     // achromatomaly
    };

    private int cameraVertexShader;

}
