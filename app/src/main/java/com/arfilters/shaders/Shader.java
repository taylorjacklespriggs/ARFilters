package com.arfilters.shaders;

import android.opengl.GLES20;
import android.util.Log;

import com.arfilters.GLTools;
import com.arfilters.shaders.data.Float3Data;
import com.arfilters.shaders.data.FloatBufferData;
import com.arfilters.shaders.data.FloatData;
import com.arfilters.shaders.data.IntegerData;
import com.arfilters.shaders.data.Matrix3x3ArrayData;
import com.arfilters.shaders.data.Matrix3x3Data;
import com.arfilters.shaders.data.TextureLocationData;
import com.arfilters.shaders.data.VertexAttributeData;
import com.arfilters.shaders.variable.Attribute;
import com.arfilters.shaders.variable.ShaderVariable;
import com.arfilters.shaders.variable.Uniform;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by taylor on 12/13/16.
 */

public class Shader {

    public static final String TAG = "Shader";

    public void addUniform(String name) {
        uniforms.put(name, new Uniform(name, finalProgram));
    }

    public void createVertices(String positionName, int positionDim, FloatBuffer positions,
                               String texCoordName, int texCoordDim, FloatBuffer texCoords,
                               int length) {
        Attribute pos = new Attribute(positionName, finalProgram);
        pos.setData(new VertexAttributeData(positionDim, positions));
        vertexAttributes.add(pos);

        Attribute tex = new Attribute(texCoordName, finalProgram);
        tex.setData(new VertexAttributeData(texCoordDim, texCoords));
        vertexAttributes.add(tex);

        drawLength = length;
    }

    private void updateFloatBufferValue(ShaderVariable sv, FloatBuffer fb) {
        FloatBufferData fbd = (FloatBufferData)sv.getData();
        FloatBuffer oldFb = fbd.buffer;
        if(fbd.buffer != fb) {
            fbd.buffer = fb;
        }
    }

    public void updateMatrix3x3Uniform(String name, FloatBuffer fb) {
        ShaderVariable sv = uniforms.get(name);
        if(sv.hasData()) {
            updateFloatBufferValue(sv, fb);
        } else {
            sv.setData(new Matrix3x3Data(fb));
        }
    }

    public void updateFloat3Uniform(String name, FloatBuffer fb) {
        ShaderVariable sv = uniforms.get(name);
        if(sv == null) {
            Log.e(TAG, "Could not update uniform "+name+", spelling mistake?");
            return;
        }
        if(sv.hasData()) {
            updateFloatBufferValue(sv, fb);
        } else {
            sv.setData(new Float3Data(fb));
        }
    }

    public void updateFloatUniform(String name, float val) {
        ShaderVariable sv = uniforms.get(name);
        if(sv.hasData()) {
            FloatData tld = (FloatData)sv.getData();
            tld.value = val;
        } else {
            sv.setData(new FloatData(val));
        }
    }

    public void updateMatrix3x3ArrayUniform(String name, FloatBuffer fb, int count) {
        ShaderVariable sv = uniforms.get(name);
        if(sv == null) {
            Log.e(TAG, "Could not update uniform "+name+", spelling mistake?");
            return;
        }
        if(sv.hasData()) {
            updateFloatBufferValue(sv, fb);
        } else {
            sv.setData(new Matrix3x3ArrayData(fb, count));
        }
    }

    public void updateIntegerUniform(String name, int val) {
        ShaderVariable sv = uniforms.get(name);
        if(sv.hasData()) {
            IntegerData tld = (IntegerData)sv.getData();
            tld.value = val;
        } else {
            sv.setData(new IntegerData(val));
        }
    }

    public void updateTextureUniform(String name, int glparam, int texNum, int texLoc) {
        ShaderVariable sv = uniforms.get(name);
        if(sv.hasData()) {
            TextureLocationData tld = (TextureLocationData)sv.getData();
            tld.textureLocation = texLoc;
        } else {
            sv.setData(new TextureLocationData(glparam, texNum, texLoc));
        }
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
