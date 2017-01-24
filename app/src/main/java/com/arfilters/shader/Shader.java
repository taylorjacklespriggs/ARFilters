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

import com.arfilters.GLTools;
import com.arfilters.shader.data.IntegerData;
import com.arfilters.shader.data.ShaderData;
import com.arfilters.shader.data.TextureLocationData;
import com.arfilters.shader.data.VertexAttributeData;
import com.arfilters.shader.variable.Attribute;
import com.arfilters.shader.variable.ShaderVariable;
import com.arfilters.shader.variable.Uniform;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Shader {

    public static final String TAG = "Shader";

    public void addUniform(String name, ShaderData data) {
        uniforms.put(name, new Uniform(name, finalProgram, data));
    }

    public void createVertices(String positionName, VertexAttributeData verts,
                               String texCoordName, VertexAttributeData texCoords,
                               int length) {
        Attribute pos = new Attribute(positionName, finalProgram, verts);
        vertexAttributes.add(pos);

        Attribute tex = new Attribute(texCoordName, finalProgram, texCoords);
        vertexAttributes.add(tex);

        drawLength = length;
    }

    public void draw() {
        GLTools.checkGLError(TAG, "enter draw");

        GLES20.glUseProgram(finalProgram);
        for(Attribute att: vertexAttributes) {
            att.update();
        }
        GLTools.checkGLError(TAG, "update attributes");

        for(ShaderVariable sv: uniforms.values()) {
            sv.update();
        }
        GLTools.checkGLError(TAG, "update uniforms");

        for(Attribute att: vertexAttributes) {
            att.enable();
        }
        GLTools.checkGLError(TAG, "enable attributes");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, drawLength);
        GLTools.checkGLError(TAG, "draw triangles");

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
        vertexAttributes = new ArrayList<>();
    }

    private final int finalProgram;
    private HashMap<String, Uniform> uniforms;
    private ArrayList<Attribute> vertexAttributes;
    private int drawLength;
}