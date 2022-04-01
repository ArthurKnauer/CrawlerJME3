#extension GL_EXT_geometry_shader : enable

uniform float m_LPVTextureDepthHalved;

varying in vec2 texCoordGS[1]; // passed from the vertex shader
varying in vec3 normalGS[1];
varying out vec2 texCoord; // passing to fragment shader
varying out vec3 normal;

layout (points) in;
layout (points, max_vertices = 1) out;

void main() 
{
    // pass on vectors from the vertex shader to the fragment shader
    texCoord = texCoordGS[0];
    normal = normalGS[0]; 
    
    gl_Position = vec4(gl_PositionIn[0].xy, 0.0, 1.0);
    // layer within 3D texture
    gl_Layer = int((gl_PositionIn[0].z + 1) * m_LPVTextureDepthHalved);
  
    gl_PointSize = 1;
    EmitVertex();
    EndPrimitive();
}