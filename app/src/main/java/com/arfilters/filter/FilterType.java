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

import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.taylorjs.hproject.arfilters.R;

enum FilterType {

    PASS_THROUGH,


    ZOOM,


    ANAGLYPH,


    HUE_ROTATION,
    PROTANOPIA,
    PROTANOMALY,
    DEUTERANOPIA,
    DEUTERANOMALY,
    TRITANOPIA,
    TRITANOMALY,
    ACHROMATOPSIA,
    ACHROMATOMALY,


    INVERTED,

    GRAY_EDGES,
    GRADIENT_EDGES,
    ENHANCED_EDGES,
    TOON;

    public Shader generateShader(ShaderGenerator sg) {
        switch(getClassType()) {
            case COLOR_MAP:
                return sg.generateModifiedColorShader(
                        getFSResourceID(), false);
            case TEXTURE_WARP:
                return sg.generateModifiedTextureCoordinatesShader(
                        getFSResourceID(), com.arfilters.shader.Precision.HIGH);
            case EDGES:
                return sg.generateModifiedColorShader(getFSResourceID(), true);
            case COLOR_MOD:
                return sg.generateModifiedColorShader(getFSResourceID(), false);
            default:
                return sg.generateDefaultShader();
        }
    }

    public FilterClass getClassType() {
        switch(this) {
            case PASS_THROUGH:
                return FilterClass.PLAIN;
            case ZOOM:
                return FilterClass.TEXTURE_WARP;
            case HUE_ROTATION:
            case ANAGLYPH:
                return FilterClass.COLOR_MAP;
            case INVERTED:
                return FilterClass.COLOR_MOD;
            case GRAY_EDGES:
            case GRADIENT_EDGES:
            case ENHANCED_EDGES:
            case TOON:
                return FilterClass.EDGES;
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
            case PASS_THROUGH:
                return R.raw.passthrough;
            case ZOOM:
                return R.raw.zoomed_texture_coordinates;
            case INVERTED:
                return R.raw.inverted;
            case GRAY_EDGES:
                return R.raw.grey_edges;
            case GRADIENT_EDGES:
                return R.raw.gradient_edges;
            case ENHANCED_EDGES:
                return R.raw.enhanced_edges;
            case TOON:
                return R.raw.toon;
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
