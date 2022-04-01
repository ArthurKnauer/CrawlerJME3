uniform sampler2D m_DiffuseMap;
uniform sampler2D m_NormalMap;
uniform sampler2D m_DepthMap;

uniform sampler2D m_FlashLight; 

uniform sampler3D m_LPVRed;
uniform sampler3D m_LPVGreen;
uniform sampler3D m_LPVBlue;
uniform vec3 m_LPVMinPos;
uniform vec3 m_LPVScale;
uniform vec3 m_LPVCellSize;

uniform float m_Time;
uniform float m_LPVFluxScale;

uniform sampler2DShadow m_ShadowMap; // for direct light shadow lookup
uniform mat4 m_LightViewProjectionMatrix;
uniform vec3 m_LightDirNeg;

uniform sampler2DShadow m_ShadowMapFL; // for direct light shadow lookup
uniform mat4 m_LightViewProjectionMatrixFL;
uniform vec3 m_LightDirNegFL;
uniform vec3 m_LightPosFL;

uniform vec2 m_ProjectionValues;	// for depth to world pos calculation
uniform vec3 m_CameraPos;

varying vec2 texCoord;
varying vec3 viewRay;	// interpolated ray from the frustum rays in worldSpace (going from the eye/campos towards current pixel)

#define SH_C0 0.282094792 // 1.0 / 2 sqrt(pi)
#define SH_C1 0.488602512 // sqrt(3) / 2 sqrt(pi)

vec4 debugValue(in float value);

void main()
{
	vec4 color = texture2D(m_DiffuseMap, texCoord);	
	vec3 normal = texture2D(m_NormalMap, texCoord).xyz * 2  - 1; // map normal from [0, 1] to [-1, 1] (RGBA8 values are 0-255)

	// calculate world position from depth map 
	float zNDC = texture2D(m_DepthMap, texCoord).r;	// TODO: use linear depth? some artifacts may occur
	float zView = -m_ProjectionValues.y / (zNDC + m_ProjectionValues.x); // reproject normalized depth to [zNear, zFar]
	vec3 worldPos = m_CameraPos + viewRay * zView;

	// direct light with shadowMap: project wordPos to shadowMap texcoords
	vec4 shadowTexCoord = m_LightViewProjectionMatrix * vec4(worldPos.xyz, 1);
    float shadow = max(0.0, textureProj(m_ShadowMap, shadowTexCoord));
    float NdotL = dot(normal, m_LightDirNeg);
    shadow = max(NdotL * shadow, 0.0);

    vec4 shadowTexCoordFL = m_LightViewProjectionMatrixFL * vec4(worldPos.xyz, 1);
    float shadowFL = max(0.0, textureProj(m_ShadowMapFL, shadowTexCoordFL));
    NdotL = min(1, dot(normal, m_LightDirNegFL) + 0.5);
    shadowFL = max(NdotL * shadowFL, 0.0);

    vec2 flashLightTexCoord = shadowTexCoordFL.xy / shadowTexCoordFL.w;
	vec4 flashLightLum = texture2D(m_FlashLight, flashLightTexCoord);    

    float dist = length(m_LightPosFL - worldPos); 
    float distSquared = dist * dist;

     if (shadowTexCoordFL.w < 0)
        shadowFL = 0;  

    float att = 1 / (1 + 0.125 * dist + 0.0125 * distSquared);
    vec4 shadowFLColored = shadowFL * 1 * att * flashLightLum;   
    shadowFLColored = shadowFLColored * shadowFLColored * 0.5;

	// indirect light with LPV: calculate SH at this worldPos
    vec4 shIntensity = vec4(SH_C0, SH_C1 * normal.y, SH_C1 * -normal.z, SH_C1 * normal.x);

	vec3 lpvTexCoord = vec3((worldPos.x - m_LPVMinPos.x) / m_LPVScale.x, 
                       (worldPos.y - m_LPVMinPos.y) / m_LPVScale.y, 
                       (worldPos.z - m_LPVMinPos.z) / m_LPVScale.z);

    // this lpv cell could be occluded by geometry, since there is this poly fragment -> read next cell towards normal
	lpvTexCoord += m_LPVCellSize * normal * 0.5;

    vec4 r = texture(m_LPVRed,    lpvTexCoord);
    vec4 g = texture(m_LPVGreen,  lpvTexCoord);
    vec4 b = texture(m_LPVBlue,   lpvTexCoord);

    vec4 indirectRad = m_LPVFluxScale * vec4(max(dot(shIntensity, r), 0),
												max(dot(shIntensity, g), 0),
												max(dot(shIntensity, b), 0), 0);
    indirectRad = abs(indirectRad) * 1.0;

    vec4 ambient = vec4(0.0001, 0.0001f, 0.0001, 1);

	//float rnd = rand(texCoord + m_Time) * 0.3 + 0.85;
    vec4 fragColor = color * (shadow + indirectRad + shadowFLColored + ambient);
    gl_FragColor = fragColor;
    //gl_FragColor = vec4(normal, 1);
	//gl_FragColor = debugValue(r.y);
	gl_FragDepth = zNDC * 0.5 + 0.5; // map depth from [-1, 1] to [0, 1]
}

float rand(vec2 seed) {
    return fract(sin(dot(seed.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

vec4 debugValue(in float value) {
	if (value > 3) return vec4(1,1,1,1);
	else if (value > 2) return vec4(1,1,value - 2,1);		
	else if (value > 1) return vec4(value - 1,1,0,1);
	else if (value > 0) return vec4(0,value,0,1);
	else if (value > -1) return vec4(-value,0,1,1);
	else return vec4(0,0,1,1);
}