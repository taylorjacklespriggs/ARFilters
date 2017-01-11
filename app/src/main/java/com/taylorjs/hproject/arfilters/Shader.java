package com.taylorjs.hproject.arfilters;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by taylor on 12/13/16.
 */

public class Shader {
    public static final String TAG = "Shader";

    private abstract class ShaderData {
        public abstract void update(int location);
        public void enable(int location) {}
        public void disable(int location) {}
    }

    private abstract class FloatBufferData extends ShaderData {
        public FloatBuffer buffer;
        public FloatBufferData(FloatBuffer fb) {
            buffer = fb;
        }
    }

    private class Matrix3x3Data extends FloatBufferData {
        public Matrix3x3Data(FloatBuffer fb) {
            super(fb);
        }
        @Override
        public void update(int location) {
            GLES20.glUniformMatrix3fv(location, 1, false, buffer);
        }
    }

    private class Matrix3x3ArrayData extends FloatBufferData {
        int length;
        public Matrix3x3ArrayData(FloatBuffer fb, int count) {
            super(fb);
            length = count;
        }
        @Override
        public void update(int location) {
            GLES20.glUniformMatrix3fv(location, length, false, buffer);
        }
    }

    private class Float3Data extends FloatBufferData {
        public Float3Data(FloatBuffer fb) {
            super(fb);
        }
        @Override
        public void update(int location) {
            GLES20.glUniform3fv(location, 1, buffer);
        }
    }

    private class VertexAttributeData extends FloatBufferData {
        public final int dimensions;
        public VertexAttributeData(int dim, FloatBuffer fb) {
            super(fb);
            dimensions = dim;
        }
        @Override
        public void update(int location) {
            GLES20.glVertexAttribPointer(
                    location, dimensions, GLES20.GL_FLOAT, false, 0, buffer);
        }
        @Override
        public void enable(int location) {
            GLES20.glEnableVertexAttribArray(location);
        }
        @Override
        public void disable(int location) {
            GLES20.glDisableVertexAttribArray(location);
        }
    }

    private class FloatData extends ShaderData {
        public float value;
        public FloatData(float val) {
            value = val;
        }
        public void update(int location) {
            GLES20.glUniform1f(location, value);
        }
    }

    private class IntegerData extends ShaderData {
        public int value;
        public IntegerData(int val) {
            value = val;
        }
        public void update(int location) {
            GLES20.glUniform1i(location, value);
        }
    }

    private class TextureLocationData extends ShaderData {
        public final int glParameter;
        public int textureNumber;
        public int textureLocation;
        public TextureLocationData(int glParm, int texNum, int texLoc) {
            glParameter = glParm;
            textureNumber = texNum;
            textureLocation = texLoc;
        }
        public void update(int location) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureLocation);
            GLES20.glUniform1i(location, textureNumber);
        }
    }

    private abstract class ShaderVariable {
        private int location;
        private String name;
        private ShaderData data;
        public ShaderVariable(String varName) {
            name = varName;
            location = getLocation(name);
        }
        public String getName() {
            return name;
        }
        public void update() {
            if(data != null) {
                data.update(location);
                GLTools.checkGLError(TAG, "error updating "+getName());
            }
        }
        public void enable() {
            if(data != null) {
                data.enable(location);
            }
        }
        public void disable() {
            if(data != null) {
                data.disable(location);
            }
        }
        public void setData(ShaderData sd) {
            data = sd;
        }
        public ShaderData getData() {
            return data;
        }
        public boolean hasData() {
            return data != null;
        }

        protected abstract int getLocation(String name);
    }

    private class Uniform extends ShaderVariable {
        public Uniform(String name) {
            super(name);
        }
        @Override
        protected int getLocation(String name) {
            return GLES20.glGetUniformLocation(finalProgram, name);
        }
    }

    private class Attribute extends ShaderVariable {
        public Attribute(String name) {
            super(name);
        }
        @Override
        protected int getLocation(String name) {
            return GLES20.glGetAttribLocation(finalProgram, name);
        }
    }

    private final int finalProgram;
    private HashMap<String, Uniform> uniforms;
    private ArrayList<Attribute> vertexAttributes;
    private int drawLength;

    public Shader(int vProgram, int fProgram) {
        finalProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(finalProgram, vProgram);
        GLES20.glAttachShader(finalProgram, fProgram);
        GLES20.glLinkProgram(finalProgram);
        GLES20.glUseProgram(finalProgram);
        uniforms = new HashMap<>();
        vertexAttributes = new ArrayList<>();
    }

    public void addUniform(String name) {
        uniforms.put(name, new Uniform(name));
    }

    public void createVertices(String positionName, int positionDim, FloatBuffer positions,
                               String texCoordName, int texCoordDim, FloatBuffer texCoords,
                               int length) {
        Attribute pos = new Attribute(positionName);
        pos.setData(new VertexAttributeData(positionDim, positions));
        vertexAttributes.add(pos);

        Attribute tex = new Attribute(texCoordName);
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
}
