uniform sampler3D m_GV;

uniform sampler3D m_LPVRed;
uniform sampler3D m_LPVGreen;
uniform sampler3D m_LPVBlue;

uniform bool m_ProbeLPV;
uniform float m_LPVFluxScale;

varying vec3 normal;
varying vec3 gvTexCoord;
varying vec3 lpvTexCoord;

#define SH_C0 0.282094792 // 1.0 / 2 sqrt(pi)
#define SH_C1 0.488602512 // sqrt(3) / 2 sqrt(pi)

vec4 getGVSample() {
//	vec4 shIntensity = vec4(SH_C0, SH_C1 * normal.y, SH_C1 * -normal.z, SH_C1 * normal.x);
    vec4 geomSH = texture(m_GV, gvTexCoord);
//    float radianceGV = dot(shIntensity, geomSH);
//	return vec4(radianceGV, radianceGV, radianceGV, 1);
	return geomSH;//vec4(length(geomSH));
}

vec4 getLPVSample() {
	vec4 shIntensity = vec4(SH_C0, SH_C1 * normal.y, SH_C1 * -normal.z, SH_C1 * normal.x);
	return vec4(dot(shIntensity,  texture(m_LPVRed, lpvTexCoord)),
				dot(shIntensity,  texture(m_LPVGreen, lpvTexCoord)),
				dot(shIntensity,  texture(m_LPVBlue, lpvTexCoord)),
				1);
}

void main() {
	//if (m_ProbeLPV) 
    //    gl_FragColor = m_LPVFluxScale * getLPVSample();
	//else 
        gl_FragColor = getGVSample();        
}