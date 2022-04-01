uniform mat4 m_WorldMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;

out vec3 WorldPos_TCS;
out vec3 Normal_TCS;

/* Transform vertices and their normals to world coordinates and pass them to the tessellation control. */
void main() {
    Normal_TCS = normalize(m_WorldMatrix * vec4(inNormal, 0)).xyz;
    WorldPos_TCS = (m_WorldMatrix * vec4(inPosition, 1.0)).xyz; 
}