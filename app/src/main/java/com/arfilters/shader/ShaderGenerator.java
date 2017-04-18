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

import android.opengl.GLES20;
import android.util.Log;

import com.arfilters.GLTools;
import com.arfilters.ResourceLoader;

import java.util.HashMap;

public class ShaderGenerator {

    private static final String TAG = "ShaderGenerator";

    public void setInitializer(ShaderInitializer si) {
        initializer = si;
    }

    public ShaderGenerator copy() {
        return new ShaderGenerator(this);
    }

    public void setUseCamera(boolean useCamera) {
        this.useCamera = useCamera;
    }

    public void setUseDerivatives(boolean useDerivatives) {
        this.useDerivatives = useDerivatives;
    }

    public void setGetTextureFragment(int getTextureFragment) {
        this.getTextureFragmentString = resourceLoader.readRawTextFile(getTextureFragment);
    }

    public void setGetTextureCoordinates(int getTextureCoordinates) {
        this.getTextureCoordinatesString = resourceLoader.readRawTextFile(getTextureCoordinates);
    }

    public void setComputeColor(int computeColorString) {
        this.computeColorString = resourceLoader.readRawTextFile(computeColorString);
    }

    public void setFloatPrecision(Precision floatPrecision) {
        this.floatPrecision = floatPrecision;
    }

    public Shader generateShader() {
        StringBuilder sb = new StringBuilder();
        if(useCamera) {
            sb.append("#extension GL_OES_EGL_image_external : require\n");
        }
        if(useDerivatives) {
            sb.append("#extension GL_OES_standard_derivatives : enable\n");
        }
        sb.append(floatPrecision.getString("float"));
        sb.append('\n');
        sb.append(getTextureFragmentString);
        sb.append('\n');
        sb.append(getTextureCoordinatesString);
        sb.append('\n');
        sb.append(computeColorString);
        sb.append('\n');
        sb.append(mainString);
        String shader = sb.toString();

        int fs;
        // check if the shader has already been compiled
        if(reusablePrograms.containsKey(shader)) {
            fs = reusablePrograms.get(shader);
        } else {
            fs = GLTools.loadGLShader(TAG, GLES20.GL_FRAGMENT_SHADER, shader);
            reusablePrograms.put(shader, fs);
        }

        Shader sh = new Shader(vertexShader, fs);
        if(initializer != null) {
            initializer.initializeShader(sh);
        }
        return sh;
    }

    public ShaderGenerator(ResourceLoader rl,
                           int vertexShader,
                           String getTextureFragmentString,
                           String getTextureCoordinatesString,
                           String computeColorString,
                           String mainString,
                           boolean useCamera,
                           boolean useDerivatives,
                           Precision floatPrecision) {
        init(rl, vertexShader, getTextureFragmentString,
                getTextureCoordinatesString, computeColorString, mainString,
                useCamera, useDerivatives, floatPrecision, null);
        reusablePrograms = new HashMap<>();
    }

    private ShaderGenerator(ShaderGenerator s) {
        init(s.resourceLoader, s.vertexShader, s.getTextureFragmentString,
                s.getTextureCoordinatesString, s.computeColorString,
                s.mainString, s.useCamera, s.useDerivatives, s.floatPrecision,
                s.initializer);
        reusablePrograms = s.reusablePrograms;
    }

    private void init(ResourceLoader rl,
                      int vertexShader,
                      String getTextureFragmentString,
                      String getTextureCoordinatesString,
                      String computeColorString,
                      String mainString,
                      boolean useCamera,
                      boolean useDerivatives,
                      Precision floatPrecision,
                      ShaderInitializer si) {
        this.resourceLoader = rl;
        this.vertexShader = vertexShader;
        this.getTextureFragmentString = getTextureFragmentString;
        this.getTextureCoordinatesString = getTextureCoordinatesString;
        this.computeColorString = computeColorString;
        this.mainString = mainString;
        this.useCamera = useCamera;
        this.useDerivatives = useDerivatives;
        this.floatPrecision = floatPrecision;
        this.initializer = si;
    }

    private ResourceLoader resourceLoader;

    private int vertexShader;

    private String  getTextureFragmentString,
                    getTextureCoordinatesString,
                    computeColorString,
                    mainString;
    private boolean useCamera,
                    useDerivatives;
    private Precision floatPrecision;

    private ShaderInitializer initializer;

    private final HashMap<String, Integer> reusablePrograms;

}
