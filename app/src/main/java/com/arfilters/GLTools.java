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

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

public class GLTools {

    public static class Framebuffer {
        private final int frameBufferID, textureID, renderBufferID;
        private final int width, height;
        protected Framebuffer(int w, int h, int interpolation, int wrapType) {
            width = w;
            height = h;
            final int fbIdx = 0, texIdx = 1, rbIdx = 2;
            int[] tmp = new int[3];
            GLES20.glGenFramebuffers(1, tmp, fbIdx);
            GLES20.glGenTextures(1, tmp, texIdx);
            GLES20.glGenRenderbuffers(1, tmp, rbIdx);
            frameBufferID = tmp[fbIdx];
            textureID = tmp[texIdx];
            renderBufferID = tmp[rbIdx];
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, tmp[fbIdx]);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tmp[texIdx]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            setTextureParameters(GLES20.GL_TEXTURE_2D, interpolation, wrapType);
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferID);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
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
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
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
