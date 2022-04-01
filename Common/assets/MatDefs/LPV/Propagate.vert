attribute vec3 inPosition;

void main()
{
    gl_Position.xyz = inPosition;
}