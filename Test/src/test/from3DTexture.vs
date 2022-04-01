#version 330 core
uniform float slice;

layout(location = 0) in vec3 position;

out vec3 texCoord;

void main()
{
    texCoord = vec3(position.xy, slice);
    gl_Position = vec4(position * 2 - 1, 1.0);
}
