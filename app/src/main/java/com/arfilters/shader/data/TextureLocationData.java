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

package com.arfilters.shader.data;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

public class TextureLocationData implements ShaderData<Integer> {

    @Override
    public void updateData(Integer texNum) {
        textureNumber = texNum;
    }

    @Override
    public void updateLocation(int location) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureLocation);
        GLES20.glUniform1i(location, textureNumber);
    }

    public TextureLocationData(int texNum, int texLoc) {
        textureNumber = texNum;
        textureLocation = texLoc;
    }

    private int textureNumber;
    private int textureLocation;

}
