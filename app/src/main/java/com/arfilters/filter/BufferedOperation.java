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

import android.opengl.GLES20;

import com.arfilters.GLTools;
import com.arfilters.shader.Shader;
import com.arfilters.shader.data.Matrix3x3Data;

abstract class BufferedOperation extends SingleShaderOperation {

    private static final String TAG = BufferedOperation.class.getName();

    private static final float[] IDENTITY = new float[] {
            1f,0f,0f,0f,1f,0f,0f,0f,1f
    };

    protected void setIdentityVertexMatrix() {
        setVertexMatrix(IDENTITY);
    }

    protected void setVertexMatrix(float[] mat) {
        vertexMatrixData.updateData(mat);
    }

    BufferedOperation(Shader pt, Matrix3x3Data vertMatData,
                      VertexMatrixUpdater ptVmi, String nm) {
        super(pt, vertMatData, ptVmi, nm);
    }

    @Override
    public final void prepareView() {

        int[] vp = new int[4];

        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, vp, 0);

        setIdentityVertexMatrix();

        int mainBuffer = GLTools.getCurrentFramebuffer();

        renderToBuffers();

        GLTools.setFramebuffer(mainBuffer);

        GLES20.glViewport(vp[0], vp[1], vp[2], vp[3]);

    }

    protected abstract void renderToBuffers();
}
