#extension GL_EXT_geometry_shader : enable

uniform float m_LPVTextureDepth;

varying out vec3 lpvTexCoord; // pass lpv tex coord on to fragment shader

layout (triangles) in;
layout (triangle_strip, max_vertices=3) out;

void main() 
{
    for (int i = 0; i < gl_VerticesIn; ++i)   
    {    
        gl_Position = vec4(gl_PositionIn[i].xy, 0.0, 1.0);
        gl_Layer = int(gl_PositionIn[i].z);
	    
        // gl_PositionIn[i] lies in [-1; 1] [-1; 1] [0; 32]
        // texel position must go from [0 to 1] on all axes (for lpv lookup)
        lpvTexCoord = gl_PositionIn[i].xyz;
        lpvTexCoord.z /= m_LPVTextureDepth;
        lpvTexCoord.xy = (lpvTexCoord.xy + 1) * 0.5;
	
        EmitVertex();
    }
    EndPrimitive();
}

