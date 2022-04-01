uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;

varying vec3 normal;
varying vec2 texCoord;

void main() {
    texCoord = inTexCoord;

	normal = normalize(g_WorldMatrix * vec4(inNormal, 0)).xyz;

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}