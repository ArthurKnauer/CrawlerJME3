uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform mat4 m_LightViewProjectionMatrix;

uniform vec3 m_LPVMinPos;
uniform vec3 m_LPVScale;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;

varying vec2 texCoord;
varying vec3 normal;
varying vec3 lpvTexCoord;
varying vec4 shadowTexCoord;

void main(){
    texCoord = inTexCoord;
    normal = normalize(g_WorldMatrix * vec4(inNormal,0)).xyz;    
    vec4 worldPos = g_WorldMatrix * vec4(inPosition, 1.0);

    shadowTexCoord = m_LightViewProjectionMatrix * worldPos;

    lpvTexCoord = vec3((worldPos.x - m_LPVMinPos.x) / m_LPVScale.x, 
                       (worldPos.y - m_LPVMinPos.y) / m_LPVScale.y, 
                       (worldPos.z - m_LPVMinPos.z) / m_LPVScale.z);

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}