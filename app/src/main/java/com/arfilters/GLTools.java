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

package com.arfilters;

import android.opengl.GLES20;
import android.util.Log;

import com.arfilters.shader.variable.Embellishment;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLU.gluErrorString;

public class GLTools {

    public static class FrameBuffer implements Embellishment {

        private static final String TAG = "FrameBuffer";

        @Override
        public void enable() {
            GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, oldFramebuffer, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferID);
        }

        @Override
        public void disable() {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, oldFramebuffer[0]);
        }

        private int[] oldFramebuffer;
        private final int frameBufferID, textureID;
        private final int width, height;
        public FrameBuffer(int w, int h, int internalFormat, int format, int storeType,
                           int interpolation, int wrapType) {

            oldFramebuffer = new int[1];

            width = w;
            height = h;
            final int fbIdx = 0;
            int[] tmp = oldFramebuffer;

            GLES20.glGenFramebuffers(1, tmp, 0);

            frameBufferID = tmp[fbIdx];

            GLTools.checkGLError(TAG, "generate framebuffer");


            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferID);


            textureID = genTexture(GLES20.GL_TEXTURE_2D, interpolation, wrapType);

            GLTools.checkGLError(TAG, "set texture parameters");

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, internalFormat, getWidth(),
                    getHeight(), 0, format, storeType, null);

            GLTools.checkGLError(TAG, "generate framebuffer texture");


            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                    textureID, 0);

            GLTools.checkGLError(TAG, "set framebuffer texture");

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

            GLTools.checkGLError(TAG, "reset framebuffer");

        }

        public int getTextureID() {
            return textureID;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public static void checkGLError(String TAG, String label) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            String info = label + ": glError " + error + ": " + gluErrorString(error);
            Log.e(TAG, info);
            throw new RuntimeException(info);
        }
    }

    private static void setTextureParameters(int textureType, int interpolation, int wrapType) {

        GLES20.glTexParameterf(textureType, GL10.GL_TEXTURE_MIN_FILTER, interpolation);
        GLES20.glTexParameterf(textureType, GL10.GL_TEXTURE_MAG_FILTER, interpolation);
        GLES20.glTexParameteri(textureType, GL10.GL_TEXTURE_WRAP_S, wrapType);
        GLES20.glTexParameteri(textureType, GL10.GL_TEXTURE_WRAP_T, wrapType);

    }

    public static int genTexture(int textureType, int interpolation, int wrapType) {
        int[] genBuf = new int[1];
        GLES20.glGenTextures(1, genBuf, 0);
        GLES20.glBindTexture(textureType, genBuf[0]);

        setTextureParameters(textureType, interpolation, wrapType);

        return genBuf[0];
    }

    public static int loadGLShader(String TAG, int type, String raw) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, raw);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            Log.e(TAG, raw);
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }
}
