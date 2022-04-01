uniform mat3 g_NormalMatrix;
uniform sampler2D m_DiffuseMap;

#ifdef NORMALMAP
    uniform sampler2D m_NormalMap;
#endif 

#ifdef AMBIENTOCCLUSIONMAP
    uniform sampler2D m_AmbientOcclusionMap;
#endif 

varying vec3 normal;
varying vec2 texCoord;
varying vec2 texCoordTransformed;
varying vec2 depth;

layout (location = 0) out vec4 dffuseOut;  
layout (location = 1) out vec4 normalOut;

void main() {
    //dffuseOut = texture2D(m_DiffuseMap, texCoordTransformed);
    dffuseOut = texture2D(m_DiffuseMap, texCoord);

    #ifdef AMBIENTOCCLUSIONMAP
        dffuseOut *= texture2D(m_AmbientOcclusionMap, texCoord).r;
    #endif
    
    #ifdef NORMALMAP   

    vec3 tangent = normalize(g_NormalMatrix[0]); 
    vec3 binormal = normalize(g_NormalMatrix[1]);
    mat3 tangentToWorld = mat3( tangent.x, binormal.x, normal.x,
                                tangent.y, binormal.y, normal.y,
                                tangent.z, binormal.z, normal.z);

   // normalOut = vec4(((texture2D(m_NormalMap, texCoordTransformed).rgb * 2.0 - 1.0) * tangentToWorld + 1) * 0.5, 1);
    normalOut = vec4(((texture2D(m_NormalMap, texCoord).rgb * 2.0 - 1.0) * tangentToWorld + 1) * 0.5, 1);
    #else
        normalOut = vec4((normal + 1) * 0.5, 1);
    #endif
    
	gl_FragDepth = depth.x / depth.y; // z / w
}