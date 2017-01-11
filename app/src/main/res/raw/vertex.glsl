attribute vec4 a_Position;
attribute vec3 a_TexCoord;

uniform mat3 u_TexCoordTransform;

varying vec2 v_TexCoord;

void main() {
   v_TexCoord = (u_TexCoordTransform*a_TexCoord).xy;
   gl_Position = a_Position;
}
