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

import com.arfilters.shader.Precision;
import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.taylorjs.hproject.arfilters.R;

enum OperationType {

    PASS_THROUGH,


    HUE_ROTATION,
    PROTANOPIA,
    DEUTERANOPIA,
    TRITANOPIA,
    INVERTED,


    GRAY_EDGES,
    CHROMATIC_EDGES,
    GRADIENT_EDGES,


    LINEAR_ZOOM,
    NONLINEAR_ZOOM,


    ANAGLYPH;

    public String getName() {
        StringBuilder sb = new StringBuilder();
        for(String word: this.toString().split("_")) {
            sb.append(Character.toTitleCase(word.charAt(0)));
            if(word.length() > 1)
                sb.append(word.substring(1).toLowerCase());
            sb.append(' ');
        }
        return sb.toString();
    }

    public Shader generateShader(ShaderGenerator sg) {
        switch(getClassType()) {
            case EDGES:
                sg.setUseDerivatives(true);
            case COLOR_MOD:
            case COLOR_MAP:
                sg.setComputeColor(getFSResourceID());
                break;
            case TEXTURE_WARP:
                sg.setGetTextureCoordinates(getFSResourceID());
                sg.setFloatPrecision(Precision.HIGH);
                break;
        }
        return sg.generateShader();
    }

    public OperationClass getClassType() {
        switch(this) {
            case PASS_THROUGH:
                return OperationClass.PLAIN;
            case LINEAR_ZOOM:
            case NONLINEAR_ZOOM:
                return OperationClass.TEXTURE_WARP;
            case HUE_ROTATION:
            case ANAGLYPH:
                return OperationClass.COLOR_MAP;
            case INVERTED:
                return OperationClass.COLOR_MOD;
            case GRAY_EDGES:
            case GRADIENT_EDGES:
            case CHROMATIC_EDGES:
                return OperationClass.EDGES;
        }

        if(isColorblindType()) {
            return OperationClass.COLOR_MAP;
        }

        return OperationClass.UNKNOWN;
    }

    private int getFSResourceID() {
        if(getClassType() == OperationClass.COLOR_MAP) {
            return R.raw.color_map;
        }

        switch(this) {
            case PASS_THROUGH:
                return R.raw.passthrough;
            case LINEAR_ZOOM:
                return R.raw.linear_zoomtexcoords;
            case NONLINEAR_ZOOM:
                return R.raw.nonlinear_zoomtexcoords;
            case INVERTED:
                return R.raw.inverted;
            case GRAY_EDGES:
                return R.raw.gray_edges;
            case GRADIENT_EDGES:
                return R.raw.gradient_edges;
            case CHROMATIC_EDGES:
                return R.raw.chromatic_edges;
        }

        throw new RuntimeException("FS resource undefined for filter "+this);
    }

    public boolean isColorblindType() {
        return ordinal() >= OperationType.PROTANOPIA.ordinal()
            && ordinal() <= OperationType.TRITANOPIA.ordinal();
    }

}
