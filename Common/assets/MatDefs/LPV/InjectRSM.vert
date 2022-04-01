uniform sampler2D m_PositionMap;
uniform sampler2D m_NormalMap;

uniform vec3 m_LPVMinPos;
uniform vec3 m_LPVScale;
uniform vec3 m_LPVCellSize;

attribute vec3 inPosition;

varying out vec3 normalGS; // passing to geometry shader
varying out vec2 texCoordGS;

void main()
{
    // inPosition.xy lies in [0 1] x [0 1]
    vec3 worldPos = texture2D(m_PositionMap, inPosition.xy).xyz;

    vec3 lpvPos = vec3( (worldPos.x - m_LPVMinPos.x) / m_LPVScale.x, 
                        (worldPos.y - m_LPVMinPos.y) / m_LPVScale.y, 
                        (worldPos.z - m_LPVMinPos.z) / m_LPVScale.z);       

    normalGS = texture2D(m_NormalMap, inPosition.xy).xyz;
    texCoordGS = inPosition.xy;  

    // lpvPos must lie in [-1;1]^3                       
    lpvPos = (lpvPos * 2) - vec3(1);

    // move a cell size away from the surface to avoid illumination of near behind-backfacing polys 
    // (e.g. two sides of a thin wall, where one side is lit)
    lpvPos = lpvPos + normalGS * m_LPVCellSize * 0.5;

    gl_Position.xyz = lpvPos;
}