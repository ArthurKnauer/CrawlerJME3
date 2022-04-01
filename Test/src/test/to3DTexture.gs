#version 330 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

out vec3 color;
in int vInstance[3];

void main() {
    color = vec3(vInstance[0] / 16.0, 1.0 - vInstance[0] / 16.0, 0);  
    gl_Layer = vInstance[0];
    for (int i = 0; i < 3; i++) {
        
        gl_Position = vec4(gl_in[i].gl_Position.x, gl_in[i].gl_Position.y, 0, 1);
        EmitVertex();
    } 
   EndPrimitive();
}