uniform sampler3D m_Texture;
uniform float m_SliceCoord;
varying vec2 texCoord;

void main() {
    vec4 texVal = texture(m_Texture, vec3(texCoord.xy, m_SliceCoord));
    gl_FragColor = texVal;
}

