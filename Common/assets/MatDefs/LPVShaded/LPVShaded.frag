uniform sampler2D m_DiffuseMap;
uniform sampler2DShadow m_ShadowMap; // for hardware shadow lookup

uniform sampler3D m_LPVRed;
uniform sampler3D m_LPVGreen;
uniform sampler3D m_LPVBlue;

uniform sampler3D m_GV;

uniform vec3 m_LightDirNeg;

varying vec3 normal;
varying vec2 texCoord;
varying vec4 shadowTexCoord;
varying vec3 lpvTexCoord;

#define SH_C0 0.282094792 // 1.0 / 2 sqrt(pi)
#define SH_C1 0.488602512 // sqrt(3) / 2 sqrt(pi)

#define SH_COS_LOBE_0 0.8862269255  // sqrt(pi/3)
#define SH_COS_LOBE_1 1.0233267079 // sqrt(pi) / 2


struct SHCoeffs {
    vec4 red, green, blue;
};

void shAdd(inout SHCoeffs a, const in SHCoeffs b)
{
    a.red += b.red;
    a.green += b.green;
    a.blue += b.blue;
}

vec3 shDot(const in SHCoeffs a, const in vec4 b)
{
    return vec3(dot(a.red, b), dot(a.green, b), dot(a.blue, b));
}

vec4 shRotate(const in vec3 dir, const in vec2 vZHCoeffs)
{
    // compute sine and cosine of thetta angle
    // beware of singularity when both x and y are 0 (no need to rotate at all)
    vec2 theta12_cs = normalize(dir.xy);
    // compute sine and cosine of phi angle
    vec2 phi12_cs;
    phi12_cs.x = sqrt(1.0 - dir.z * dir.z);
    phi12_cs.y = dir.z;
    vec4 vResult;
    // The first band is rotation-independent
    vResult.x =  vZHCoeffs.x;
    // rotating the second band of SH
    vResult.y =  vZHCoeffs.y * phi12_cs.x * theta12_cs.y;
    vResult.z = -vZHCoeffs.y * phi12_cs.y;
    vResult.w =  vZHCoeffs.y * phi12_cs.x * theta12_cs.x;
    return vResult;
}

vec4 shProjectCone(const in vec3 dir, const in float angle)
{
    const vec2 vZHCoeffs = vec2(0.5 * (1.0 - cos(angle)), 0.75 * sin(angle) * sin(angle));

    return shRotate(dir, vZHCoeffs);
}

vec4 shProjectCone90Deg(const in vec3 dir)
{
    return shProjectCone(dir, 0.78539816339745);
}

vec4 shProjectCone(const in vec3 dir)
{
    return shRotate(dir, vec2(0.25, 0.5));
}

void main()
{
    float shadow = textureProj(m_ShadowMap, shadowTexCoord);

    float NdotL = dot(normal, m_LightDirNeg);
    shadow = max(NdotL * shadow, 0.0);

	vec4 color = texture2D(m_DiffuseMap, texCoord);

    vec4 shIntensity = vec4(SH_C0, SH_C1 * normal.y, SH_C1 * -normal.z, SH_C1 * normal.x);

    vec4 r = texture3D(m_LPVRed,    lpvTexCoord);
    vec4 g = texture3D(m_LPVGreen,  lpvTexCoord);
    vec4 b = texture3D(m_LPVBlue,   lpvTexCoord);

    vec4 indirectRad = vec4(dot(shIntensity, r),
                            dot(shIntensity, g),
                            dot(shIntensity, b),
                            0);

	gl_FragColor = color * (shadow + (indirectRad + 0.05) / 1.5);
	gl_FragColor.a = color.a;
}