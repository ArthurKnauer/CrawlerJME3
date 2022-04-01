uniform mat4 m_WorldMatrix;

uniform vec3 m_LPVMinPos;
uniform vec3 m_LPVScale;
uniform vec3 m_LPVCellSize;

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 normalGS;
varying vec3 lpvPos;

void main()
{
    normalGS = -normalize(m_WorldMatrix * vec4(inNormal,0) + vec4(0,-1,0,0)).xyz;
    lpvPos = (m_WorldMatrix * vec4(inPosition, 1.0)).xyz;

    lpvPos = vec3((lpvPos.x - m_LPVMinPos.x) / m_LPVScale.x,
                  (lpvPos.y - m_LPVMinPos.y) / m_LPVScale.y,
                  (lpvPos.z - m_LPVMinPos.z) / m_LPVScale.z);

    // lpvPos must lie in [-1;1]^3
    lpvPos = (lpvPos * 2) - vec3(1);

    lpvPos += normalGS * m_LPVCellSize * 1.0;

    gl_Position.xyz = lpvPos;
}