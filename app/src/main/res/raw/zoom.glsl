#extension GL_OES_EGL_image_external : require
precision highp float;

uniform samplerExternalOES u_Camera;

varying vec2 v_TexCoord;

float cosh(float x) {
    return .5*(exp(x)+exp(-x));
}

vec2 transform(vec2 coord) {
    float v1 = .02;
    float v2 = 1.;
    float v3 = 10.;
    float lensq = dot(coord,coord);
    lensq = v3*((sqrt(lensq+v2)-sqrt(v2))/sqrt(lensq)+v1);
    return lensq*coord;
}

void main() {
    vec2 newCoord = v_TexCoord;
    newCoord -= .5;
    newCoord = transform(newCoord);
    newCoord += .5;
    if(newCoord.x < 0. || newCoord.x > 1.
        || newCoord.y < 0. || newCoord.y > 1.) {
        gl_FragColor = vec4(vec3(0.), 1.);
    } else {
        gl_FragColor = texture2D(u_Camera, newCoord);
    }
}
