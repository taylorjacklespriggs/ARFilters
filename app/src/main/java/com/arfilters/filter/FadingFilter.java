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

import com.arfilters.shader.Shader;
import com.arfilters.shader.ShaderGenerator;
import com.arfilters.shader.data.FloatData;
import com.arfilters.shader.data.Matrix3x3Data;
import com.arfilters.shader.data.TextureLocationData;
import com.taylorjs.hproject.arfilters.R;

import static com.arfilters.GLTools.FrameBuffer;

class FadingFilter extends BufferedFilter {

    static FadingFilter create(ShaderGenerator camGen,
                               ShaderGenerator texGen,
                               float brightness,
                               float halfLife,
                               FrameBuffer front,
                               FrameBuffer back,
                               FrameBuffer camera,
                               Matrix3x3Data vertMatData,
                               VertexMatrixUpdater ptVmi) {
        brightness /= (float)Math.sqrt(3);
        final float fps = 60f;
        float r = fps*halfLife; // num frames
        r = (float)Math.pow(.5, 1./r); // fading factor

        // generate camera to texture shader
        Shader ctt = camGen.generateShader();

        // generate mixing shader
        texGen.setComputeColor(R.raw.monochrome_mixing_texture);
        Shader mix = texGen.generateShader();

        // create the passThrough shader
        texGen.setComputeColor(R.raw.monochrome_passthrough);
        Shader pt = texGen.generateShader();

        return new FadingFilter(ctt, mix, pt, r, brightness, front, back,
                camera, vertMatData, ptVmi);
    }

    @Override
    protected void renderToBuffers() {

        // render camera to texture
        cameraBuffer.enable();
        // now cameraBuffer has original framebufferID
        cameraToTextureShader.draw();

        // mix cameraBuffer and backBuffer
        backTextureData.newTextureLocation(backBuffer.getTextureID());
        frontBuffer.enable();
        mixingShader.draw();

        // update frontTextureID for passThrough
        frontTextureData.newTextureLocation(frontBuffer.getTextureID());

        // swap framebuffers
        FrameBuffer tmp = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = tmp;

    }

    private FadingFilter(Shader ctt, Shader mix, Shader pt, float r,
                         float brightness, FrameBuffer front, FrameBuffer back,
                         FrameBuffer camera, Matrix3x3Data vertMatData,
                         VertexMatrixUpdater ptVmi) {
        super(pt, vertMatData, ptVmi, "Fading");
        cameraToTextureShader = ctt;
        mixingShader = mix;
        frontBuffer = front;
        backBuffer = back;
        cameraBuffer = camera;
        frontTextureData = new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 0, front.getTextureID());
        backTextureData = new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 1, back.getTextureID());

        TextureLocationData camLoc = new TextureLocationData(
                GLES20.GL_TEXTURE_2D, 0, camera.getTextureID());
        FloatData secondAmount = new FloatData(r);
        mixingShader.addUniform("u_FadeAmount", secondAmount);
        mixingShader.addUniform("u_Texture", camLoc);
        mixingShader.addUniform("u_AlternateTexture", backTextureData);

        FloatData ceiling = new FloatData(1f/((1f-r)*brightness));
        pt.addUniform("u_Texture", frontTextureData);
        pt.addUniform("u_Ceiling", ceiling);
    }

    private final Shader cameraToTextureShader, mixingShader;
    private FrameBuffer frontBuffer, backBuffer;
    private final FrameBuffer cameraBuffer;
    private final TextureLocationData
            frontTextureData,
            backTextureData;

}
