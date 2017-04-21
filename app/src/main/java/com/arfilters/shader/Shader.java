/*
 *  Copyright (C) 2017  Taylor Jackle Spriggs
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
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
import com.arfilters.shader.data.ShaderData;
import com.arfilters.shader.data.VertexAttributeData;
import com.arfilters.shader.variable.Attribute;
import com.arfilters.shader.variable.ShaderVariable;
import com.arfilters.shader.variable.Uniform;

import java.util.HashMap;

/**
 * Stores shader information as program id, attributes and uniforms
 */
public class Shader {

    private static final String TAG = Shader.class.getName();

    public void addUniform(String name, ShaderData data) {
        uniforms.put(name, new Uniform(name, finalProgram, data));
    }

    void createVertices(String positionName, VertexAttributeData verts,
                               String texCoordName, VertexAttributeData texCoords,
                               int length) {
        Attribute pos = new Attribute(positionName, finalProgram, verts);
        vertexAttributes[0] = pos;

        Attribute tex = new Attribute(texCoordName, finalProgram, texCoords);
        vertexAttributes[1] = tex;

        drawLength = length;
    }

    private void initialize() {

        GLTools.checkGLError(TAG, "enter initialize");

        GLES20.glUseProgram(finalProgram);

        GLTools.checkGLError(TAG, "swapped programs");

        updateAttributes();

        updateUniforms();

        for(Attribute att: vertexAttributes) {
            att.enable();
        }
        GLTools.checkGLError(TAG, "enable attributes");

    }

    public void draw() {

        initialize();

        GLTools.checkGLError(TAG, "enter draw");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, drawLength);

        GLTools.checkGLError(TAG, "draw triangles");

        exit();

    }

    public Shader clone() {
        return new Shader(this);
    }

    private void exit() {

        GLTools.checkGLError(TAG, "enter exit");

        for(Attribute att: vertexAttributes) {
            att.disable();
        }

        GLTools.checkGLError(TAG, "disable attributes");

    }

    public Shader(int vProgram, int fProgram) {
        finalProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(finalProgram, vProgram);

        GLES20.glAttachShader(finalProgram, fProgram);

        GLES20.glLinkProgram(finalProgram);

        GLES20.glUseProgram(finalProgram);

        uniforms = new HashMap<>();
    }

    private Shader(Shader other) {
        finalProgram = other.finalProgram;
        uniforms = new HashMap<>(other.uniforms);
        for(int i = 0; i < vertexAttributes.length; ++i)
            vertexAttributes[i] = other.vertexAttributes[i];
        drawLength = other.drawLength;
    }

    private void updateUniforms() {

        for(ShaderVariable sv: uniforms.values()) {
            sv.update();
        }
        GLTools.checkGLError(TAG, "update uniforms");

    }

    private void updateAttributes() {

        for(Attribute att: vertexAttributes) {
            att.update();
        }
        GLTools.checkGLError(TAG, "update attributes");

    }

    private final int finalProgram;
    private HashMap<String, Uniform> uniforms;
    private final Attribute[] vertexAttributes = new Attribute[2];
    private int drawLength;
}
