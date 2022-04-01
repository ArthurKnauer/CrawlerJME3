uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

uniform vec3 m_GVMinPos;
uniform vec3 m_LPVMinPos;
uniform vec3 m_GVScale;

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 normal;
varying vec3 gvTexCoord;
varying vec3 lpvTexCoord;

void main(){
    normal = normalize(g_WorldMatrix * vec4(inNormal,0)).xyz;    
    vec4 worldPos = g_WorldMatrix * vec4(inPosition, 1.0);

    gvTexCoord = vec3((worldPos.x - m_GVMinPos.x) / m_GVScale.x, 
                       (worldPos.y - m_GVMinPos.y) / m_GVScale.y, 
                       (worldPos.z - m_GVMinPos.z) / m_GVScale.z);

	lpvTexCoord = vec3((worldPos.x - m_LPVMinPos.x) / m_GVScale.x, 
                       (worldPos.y - m_LPVMinPos.y) / m_GVScale.y, 
                       (worldPos.z - m_LPVMinPos.z) / m_GVScale.z);

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}