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

import com.arfilters.GLTools;
import com.arfilters.ResourceLoader;

public class ShaderGenerator {

    public static final String TAG = "ShaderGenerator";

    public Shader generateDefaultShader() {
        return generateShader(getTextureFragmentString, getTextureCoordinatesString,
                computeColorString, mainString, useCamera, useDerivatives, floatPrecision);
    }

    public Shader generateModifiedTextureFragmentShader(int getTextureFragmentID,
                                                        boolean useCamera) {
        return generateShader(resourceLoader.readRawTextFile(getTextureFragmentID),
                this.getTextureCoordinatesString, this.computeColorString, this.mainString,
                useCamera, this.useDerivatives, this.floatPrecision);
    }

    public Shader generateModifiedTextureCoordinatesShader(int getTextureCoordinatesID,
                                                           Precision floatPrecision) {
        return generateShader(this.getTextureFragmentString,
                resourceLoader.readRawTextFile(getTextureCoordinatesID), this.computeColorString,
                this.mainString, this.useCamera, this.useDerivatives, floatPrecision);
    }

    public Shader generateModifiedColorShader(int computeColorID, boolean useDerivatives) {
        return generateShader(this.getTextureFragmentString,
                this.getTextureCoordinatesString, resourceLoader.readRawTextFile(computeColorID),
                this.mainString, this.useCamera, useDerivatives, this.floatPrecision);
    }

    public Shader generateModifiedMainShader(int mainID) {
        return generateShader(this.getTextureFragmentString, this.getTextureCoordinatesString,
                this.computeColorString, resourceLoader.readRawTextFile(mainID), this.useCamera,
                useDerivatives, this.floatPrecision);
    }

    public ShaderGenerator(ResourceLoader rl, int vertexShader, String getTextureFragmentString,
                           String getTextureCoordinatesString, String computeColorString,
                           String mainString, boolean useCamera, boolean useDerivatives,
                           Precision floatPrecision) {
        this.resourceLoader = rl;
        this.vertexShader = vertexShader;
        this.getTextureFragmentString = getTextureFragmentString;
        this.getTextureCoordinatesString = getTextureCoordinatesString;
        this.computeColorString = computeColorString;
        this.mainString = mainString;
        this.useCamera = useCamera;
        this.useDerivatives = useDerivatives;
        this.floatPrecision = floatPrecision;
    }

    private Shader generateShader(String gtfs, String gtcs, String ccs, String ms,
                                         boolean uc, boolean ud, Precision fp) {
        StringBuilder sb = new StringBuilder();
        if(uc) {
            sb.append("#extension GL_OES_EGL_image_external : require\n");
        }
        if(ud) {
            sb.append("#extension GL_OES_standard_derivatives : enable\n");
        }
        sb.append(fp.getString("float"));
        sb.append('\n');
        sb.append(gtfs);
        sb.append('\n');
        sb.append(gtcs);
        sb.append('\n');
        sb.append(ccs);
        sb.append('\n');
        sb.append(ms);
        int fs = GLTools.loadGLShader(TAG, GLES20.GL_FRAGMENT_SHADER, sb.toString());

        return new Shader(vertexShader, fs);
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

}
