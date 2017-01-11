#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES u_Camera;
uniform int u_Index;
uniform mat3 u_ColourMaps[9];

varying vec2 v_TexCoord;
void main() {
    if(v_TexCoord.x*(1.-v_TexCoord.x) < 0.
        || v_TexCoord.y*(1.-v_TexCoord.y) < 0.) {
        gl_FragColor = vec4(vec3(0.), 1.);
    } else {
        gl_FragColor = vec4(texture2D(u_Camera, v_TexCoord).rgb*u_ColourMaps[u_Index], 1.);
    }
}
