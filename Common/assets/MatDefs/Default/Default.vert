uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 normal;
varying vec3 position;
varying vec2 depth;

void main() {
    normal = normalize(g_WorldMatrix * vec4(inNormal, 0)).xyz;
    position = inPosition;

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
	depth.x = gl_Position.z;
	depth.y = gl_Position.w;
}