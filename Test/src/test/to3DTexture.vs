#version 330 core

layout(location = 0) in vec3 position;

out int vInstance;

void main()
{
    gl_Position = vec4(position.xy * 2 - 1, position.z, 1.0);
    vInstance = gl_InstanceID;
}
