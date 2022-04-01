uniform sampler2D m_DiffuseMap;
uniform vec3 m_SunLightDir;

varying vec3 normal;
varying vec2 texCoord;

const float C1 = 0.429043;
const float C2 = 0.511664;
const float C3 = 0.743125;
const float C4 = 0.886227;
const float C5 = 0.247708;

// eucalyptus groove light probe
const vec3 L00  = vec3( 0.38,  0.43,  0.45);
const vec3 L1m1 = vec3( 0.29,  0.36,  0.41);
const vec3 L10  = vec3( 0.04,  0.03,  0.01);
const vec3 L11  = vec3(-0.10, -0.10, -0.09);
const vec3 L2m2 = vec3(-0.06, -0.06, -0.04);
const vec3 L2m1 = vec3( 0.01, -0.01,  0.05);
const vec3 L20  = vec3(-0.09, -0.13, -0.15);
const vec3 L21  = vec3(-0.06, -0.05, -0.04);
const vec3 L22  = vec3(-0.02, -0.00, -0.05);

void main()
{
    vec3 tnorm = normalize(normal);

    float light = max(0.3, dot(tnorm, m_SunLightDir));

    vec4 texColor = texture2D(m_DiffuseMap, texCoord);	
    
	vec3 diffuseColor =  C1 * L22 * (tnorm.x * tnorm.x - tnorm.y * tnorm.y) +
                    C3 * L20 * tnorm.z * tnorm.z +
                    C4 * L00 -
                    C5 * L20 +
                    2.0 * C1 * L2m2 * tnorm.x * tnorm.y +
                    2.0 * C1 * L21  * tnorm.x * tnorm.z +
                    2.0 * C1 * L2m1 * tnorm.y * tnorm.z +
                    2.0 * C2 * L11  * tnorm.x +
                    2.0 * C2 * L1m1 * tnorm.y +   
                    2.0 * C2 * L10  * tnorm.z;
  
    gl_FragColor = vec4(diffuseColor * light, 1) * texColor * 1.5;
}