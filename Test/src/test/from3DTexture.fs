#version 330 core
uniform sampler3D texture3d ;

out vec4 fragColor;
in vec3 texCoord;

void main()
{
    fragColor =  texture(texture3d, texCoord); 
}
