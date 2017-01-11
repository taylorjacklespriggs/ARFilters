#extension GL_OES_EGL_image_external : require
#extension GL_OES_standard_derivatives : enable

#define THRESHOLD .1
#define STRICT 20.

precision mediump float;

uniform samplerExternalOES u_Camera;

varying vec2 v_TexCoord;

float sigmoid(float varIn) {
    return 1./(1.+exp(-varIn));
}

void getEdges(out vec4 fragColour, in vec2 fragCoord) {
    float grey = length(texture2D(u_Camera, fragCoord).rgb);
    grey = length(vec2(dFdx(grey), dFdy(grey)));
    grey = sigmoid(STRICT*(grey-THRESHOLD));
    fragColour = vec4(vec3(grey), 1.);
}

void main() {
    if(v_TexCoord.x*(1.-v_TexCoord.x) < 0.
        || v_TexCoord.y*(1.-v_TexCoord.y) < 0.) {
        gl_FragColor = vec4(vec3(0.), 1.);
    } else {
        getEdges(gl_FragColor, v_TexCoord);
    }
}