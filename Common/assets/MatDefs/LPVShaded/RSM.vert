uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;

varying vec3 worldPos;
varying vec3 normal;
varying vec2 texCoord;

void main(){
    worldPos = (g_WorldMatrix * vec4(inPosition, 1)).xyz;
    normal = normalize(g_WorldMatrix * vec4(inNormal, 0)).xyz;
    texCoord = inTexCoord;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}