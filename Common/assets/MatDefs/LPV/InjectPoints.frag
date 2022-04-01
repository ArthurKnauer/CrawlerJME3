uniform float m_FluxScale;
uniform bool m_UseCosLobe;

varying vec3 normal;

#define SH_C0 0.282094792 // 1 / 2sqrt(pi)
#define SH_C1 0.488602512 // sqrt(3/pi)

#define SH_COS_LOBE_0 0.8862269255  // sqrt(pi/3)
#define SH_COS_LOBE_1 1.0233267079 // sqrt(pi) / 2

void main()
{
	// TODO: replace by sky function
    vec4 sh;
	if (m_UseCosLobe) {
		sh = vec4(SH_COS_LOBE_0, -SH_COS_LOBE_1 * normal.y, SH_COS_LOBE_1 * normal.z, -SH_COS_LOBE_1 * normal.x);
	}
	else {
		sh = vec4(SH_C0, -SH_C1 * normal.y, SH_C1 * normal.z, -SH_C1 * normal.x);
	}

    vec3 flux = vec3(1, 1, 1);

    gl_FragData[0] = flux.r * sh * m_FluxScale;
    gl_FragData[1] = flux.g * sh * m_FluxScale;
    gl_FragData[2] = flux.b * sh * m_FluxScale;
}