#extension GL_EXT_geometry_shader : enable

uniform float m_LPVTextureDepthHalved;

varying in vec3 normalGS[1]; // passed from the vertex shader
varying out vec3 normal; // passing to the fragment shader

layout (points) in;
layout (points, max_vertices = 1) out;

void main() 
{
    normal = normalGS[0]; 
    
    gl_Position = vec4(gl_PositionIn[0].xy, 0.0, 1.0);
    // layer within 3D texture
    gl_Layer = int((gl_PositionIn[0].z + 1) * m_LPVTextureDepthHalved);
  
    gl_PointSize = 1;
    EmitVertex();
    EndPrimitive();
}