uniform sampler2D m_DiffuseMap;
uniform vec3 g_CameraDirection;

varying vec3 worldPos;
varying vec3 normal;
varying vec2 texCoord;

void main() {
    gl_FragData[0] = dot(normal, -g_CameraDirection) * texture2D(m_DiffuseMap, texCoord);
    gl_FragData[1] = vec4(normal, 1);
    gl_FragData[2] = vec4(worldPos, 1);
}