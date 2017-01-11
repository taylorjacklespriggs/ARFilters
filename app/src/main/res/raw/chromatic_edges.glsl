#extension GL_OES_EGL_image_external : require
#extension GL_OES_standard_derivatives : enable

precision mediump float;

uniform samplerExternalOES u_Camera;
uniform float u_Threshold;
uniform float u_Strictness;

varying vec2 v_TexCoord;

vec3 sigmoid(vec3 varIn) {
    return 1./(1.+exp(-varIn));
}

void getEdges(out vec4 fragColour, in vec2 fragCoord) {
    vec3 colour = texture2D(u_Camera, fragCoord).rgb;
    colour = vec3(
        length(vec2(dFdx(colour.r), dFdy(colour.r))),
        length(vec2(dFdx(colour.g), dFdy(colour.g))),
        length(vec2(dFdx(colour.b), dFdy(colour.b)))
    );
    fragColour = vec4(sigmoid(u_Strictness*(colour-u_Threshold)), 1.);
}

void main() {
    if(v_TexCoord.x*(1.-v_TexCoord.x) < 0.
        || v_TexCoord.y*(1.-v_TexCoord.y) < 0.) {
        gl_FragColor = vec4(vec3(0.), 1.);
    } else {
        getEdges(gl_FragColor, v_TexCoord);
    }
}