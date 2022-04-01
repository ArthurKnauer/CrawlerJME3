uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform mat3 m_TextureTransform;
//uniform mat3 g_NormalMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;

#ifdef NORMALMAP
   //varying mat3 tangentToWorld;
#endif 

varying vec3 normal;
varying vec2 texCoord;
varying vec2 texCoordTransformed;
varying vec2 depth;

void main() {
    normal = normalize(g_WorldMatrix * vec4(inNormal, 0)).xyz;

    texCoord = inTexCoord;
    texCoordTransformed = (m_TextureTransform * vec3(inTexCoord, 1)).xy;

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
	depth.x = gl_Position.z;
	depth.y = gl_Position.w;
}