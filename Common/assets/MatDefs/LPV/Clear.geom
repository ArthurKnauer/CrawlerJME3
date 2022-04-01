layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;


void main() {
    for (int i = 0; i < 3; i++) {        
        gl_Position = vec4(gl_in[i].gl_Position.x, gl_in[i].gl_Position.y, 0, 1);
        gl_Layer = int(gl_in[i].gl_Position.z);  
        EmitVertex();
    } 
    EndPrimitive();
}