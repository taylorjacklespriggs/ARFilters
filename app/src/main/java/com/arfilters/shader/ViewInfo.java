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

import android.support.annotation.NonNull;

import com.arfilters.shader.data.Matrix3x3Data;
import com.google.vr.sdk.base.Eye;

public class ViewInfo {

    public void setEye(Eye eye) {
        setEyeType(eye.getType());
        setWidth(eye.getViewport().width);
        setHeight(eye.getViewport().height);
    }

    public void setEyeType(@NonNull int vt) {
        eyeType = vt;
    }

    public void setWidth(int w) {
        width = w;
    }

    public void setHeight(int h) {
        height = h;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getEyeType() {
        return eyeType;
    }

    public ViewInfo() {
    }

    private int eyeType;
    private int width, height;

}
