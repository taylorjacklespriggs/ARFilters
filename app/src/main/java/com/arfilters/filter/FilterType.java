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

import android.util.Log;

import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.taylorjs.hproject.arfilters.R;

public enum FilterType {

    PASS_THROUGH,


    ZOOM,


    HUE_ROTATION,

    ANAGLYPH,
    PROTANOPIA,
    PROTANOMALY,
    DEUTERANOPIA,
    DEUTERANOMALY,
    TRITANOPIA,
    TRITANOMALY,
    ACHROMATOPSIA,
    ACHROMATOMALY,


    GRAY_EDGES;

    public Shader generateShader(ShaderGenerator sg) {
        switch(getClassType()) {
            case COLOR_MAP:
                return sg.generateModifiedColorShader(
                        getFSResourceID(), false);
            case TEXTURE_WARP:
                return sg.generateModifiedTextureCoordinatesShader(
                        getFSResourceID(), com.arfilters.shader.Precision.HIGH);
            case EDGES:
                return sg.generateModifiedColorShader(
                        getFSResourceID(), true);
            default:
                return sg.generateDefaultShader();
        }
    }

    public FilterClass getClassType() {
        switch(this) {
            case PASS_THROUGH:
                return FilterClass.PLAIN;
            case HUE_ROTATION:
            case ANAGLYPH:
                return FilterClass.COLOR_MAP;
            case GRAY_EDGES:
                return FilterClass.EDGES;
            case ZOOM:
                return FilterClass.TEXTURE_WARP;
        }

        if(isColorblindType()) {
            return FilterClass.COLOR_MAP;
        }

        return FilterClass.UNKNOWN;
    }

    private int getFSResourceID() {
        if(getClassType() == FilterClass.COLOR_MAP) {
            return R.raw.color_map;
        }

        switch(this) {
            case GRAY_EDGES:
                return R.raw.grey_edges;
            case ZOOM:
                return R.raw.zoomed_texture_coordinates;
            case PASS_THROUGH:
                return R.raw.passthrough;
        }

        throw new RuntimeException("FS resource undefined for filter "+this);
    }

    public int getColorblindIndex() {
        return this.ordinal() - FilterType.PROTANOPIA.ordinal();
    }

    public boolean isColorblindType() {
        return ordinal() >= FilterType.PROTANOPIA.ordinal()
            && ordinal() <= FilterType.ACHROMATOMALY.ordinal();
    }

}
