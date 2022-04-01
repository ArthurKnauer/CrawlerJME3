#extension GL_EXT_geometry_shader : enable

uniform vec3 m_GVMinPos;
uniform vec3 m_GVScale;
uniform vec3 m_GVCellSize;
uniform float m_GVTextureDepthHalved;

in vec3 WorldPos_GS[];
in vec3 Normal_GS[];
in float Area_GS[];

out vec3 Normal_FS;
out float Area_FS;

layout (points) in;
layout (points, max_vertices = 1) out;

/* Compute GV coordinates from world coordinates, select layer, pass to FS. */
void main() {
	vec3 gvPos = vec3((WorldPos_GS[0].x - m_GVMinPos.x) / m_GVScale.x, 
                      (WorldPos_GS[0].y - m_GVMinPos.y) / m_GVScale.y, 
                      (WorldPos_GS[0].z - m_GVMinPos.z) / m_GVScale.z);  

	//gvPos.x += m_GVCellSize.x + 0.5; 

    // gvPos must lie in [-1;1]^3                       
    gvPos = (gvPos * 2) - vec3(1, 1, 1);
	//gvPos -= Normal_GS[0] * m_GVCellSize * 0.5;

    Normal_FS = Normal_GS[0]; 
	Area_FS = Area_GS[0];
    
    gl_Position = vec4(gvPos.xy, 0, 1);
    // layer within 3D texture
    gl_Layer = int((gvPos.z + 1) * m_GVTextureDepthHalved);
  
    gl_PointSize = 1;
    EmitVertex();
    EndPrimitive();
}