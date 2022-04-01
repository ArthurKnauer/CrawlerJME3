uniform sampler3D m_LPVRed;
uniform sampler3D m_LPVGreen;
uniform sampler3D m_LPVBlue;
uniform sampler3D m_GV;

uniform vec3 m_LPVCellSize;

uniform bool m_CheckOcclusion;
uniform float m_FluxScale;

varying vec3 lpvTexCoord;

#define SH_C0 0.282094792 // 1.0 / 2 sqrt(pi)
#define SH_C1 0.488602512 // sqrt(3) / 2 sqrt(pi)

#define SH_COS_LOBE_0 0.8862269255  // sqrt(pi/3)
#define SH_COS_LOBE_1 1.0233267079 // sqrt(pi) / 2

#define PI 3.14159265359


mat3x3 Prop_neighbourOrientations[6] = mat3x3[](
    // Z+
    mat3x3(
        1, 0, 0,
        0, 1, 0,
        0, 0, 1
    ),
    // Z-
    mat3x3(
        -1, 0, 0,
        0, 1, 0,
        0, 0, -1
    ),
    // X+
    mat3x3(
        0, 0, 1,
        0, 1, 0,
        -1, 0, 0
    ),
    // X-
    mat3x3(
        0, 0, -1,
        0, 1, 0,
        1, 0, 0
    ),
    // Y+
    mat3x3(
        1, 0, 0,
        0, 0, 1,
        0, -1, 0
    ),
    // Y-
    mat3x3(
        1, 0, 0,
        0, 0, -1,
        0, 1, 0
    )
);


vec3 neighbourOrientations[26] = vec3[](
	vec3( 0,  0,  1),
	vec3( 0,  0, -1),
	vec3( 0,  1,  0),
	vec3( 0, -1,  0),
	vec3( 1,  0,  0),
	vec3(-1,  0,  0),

	vec3( 1,  1,  1),
	vec3( 1,  1, -1),
	vec3( 1, -1,  1),
	vec3( 1, -1, -1),
	vec3(-1,  1,  1),
	vec3(-1,  1, -1),
	vec3(-1, -1,  1),
	vec3(-1, -1, -1),

	vec3( 1,  1, 0),
	vec3( 1, -1, 0),
	vec3(-1,  1, 0),
	vec3(-1, -1, 0),

	vec3( 1, 0,  1),
	vec3( 1, 0, -1),
	vec3(-1, 0,  1),
	vec3(-1, 0, -1),

	vec3(0,  1,  1),
	vec3(0,  1, -1),
	vec3(0, -1,  1),
	vec3(0, -1, -1)
);

struct SHcoeffs {
    vec4 red, green, blue;
};


#define SH_cosLobe_c0 0.886226925 /* sqrt(pi)/2 */
#define SH_cosLobe_c1 1.02332671 /* sqrt(pi/3) */

#define SH_c0 0.282094792 // 1 / 2sqrt(pi)
#define SH_c1 0.488602512 // sqrt(3/pi) / 2

vec4 SH_evaluate( vec3 direction ) {
    direction = normalize( direction );

    return vec4( SH_c0, -SH_c1 * direction.y, SH_c1 * direction.z, -SH_c1 * direction.x );
}

// no normalization
vec4 SH_evaluate_direct( vec3 direction ) {
    return vec4( SH_c0, -SH_c1 * direction.y, SH_c1 * direction.z, -SH_c1 * direction.x );
}

vec4 SH_evaluateCosineLobe( vec3 direction ) {
    direction = normalize( direction );

    return vec4( SH_cosLobe_c0, -SH_cosLobe_c1 * direction.y, SH_cosLobe_c1 * direction.z, -SH_cosLobe_c1 * direction.x );
}

// no normalization
vec4 SH_evaluateCosineLobe_direct( vec3 direction ) {
    return vec4( SH_cosLobe_c0, -SH_cosLobe_c1 * direction.y, SH_cosLobe_c1 * direction.z, -SH_cosLobe_c1 * direction.x );
}

// uses homogenous coordinate to become linear - and no normalization!
float SH_evaluateCosineLobe_linear( vec4 direction ) {
    return dot( vec4( SH_cosLobe_c0, -SH_cosLobe_c1, SH_cosLobe_c1, -SH_cosLobe_c1 ), direction.wyzx );
}

const vec2 Prop_side[4] = vec2[](vec2(1, 0), vec2(0, 1), vec2(-1, 0), vec2(0, -1));

// orientation = [ right | up | forward ] = [ x | y | z ]
vec3 Prop_getEvalSideDirection(int index, mat3x3 orientation ) {
    const float smallComponent = 0.4472135; // 1 / sqrt(5)
    const float bigComponent = 0.894427; // 2 / sqrt(5)

    vec2 side = Prop_side[ index ];
    // *either* x = 0 or y = 0
    return orientation * vec3(side.x * smallComponent, side.y * smallComponent, bigComponent);
}

vec3 Prop_getReprojSideDirection(int index, mat3x3 orientation ) {
    vec2 side = Prop_side[ index ];
    return orientation * vec3(side.x, side.y, 0);
}

SHcoeffs Prop_gatherContributions(vec3 cellCoord) {
    SHcoeffs contribution = SHcoeffs(vec4(0), vec4(0), vec4(0));

    for (int n = 0; n < 26; n++) {
       // mat3x3 orientation = Prop_neighbourOrientations[n];

       // vec3 mainDirection = mul(orientation, vec3(0, 0, 1));
		vec3 mainDirection = neighbourOrientations[n];
		
        vec3 neighborCoord = cellCoord - mainDirection * m_LPVCellSize * 0.5;

        SHcoeffs neighbourCoeffs = SHcoeffs(texture(m_LPVRed, neighborCoord),
                                            texture(m_LPVGreen, neighborCoord),
                                            texture(m_LPVBlue, neighborCoord));

		mainDirection = normalize(mainDirection);

        float directFaceSubtendedSolidAngle = 1.0 / 26.0; //4.0 / 18.0;
	//	if (n > 5) directFaceSubtendedSolidAngle *= 0.75;
        const float sideFaceSubtendedSolidAngle = 4.0 / 36.0;

        vec4 mainDirectionCosineLobeSH = SH_evaluateCosineLobe_direct(mainDirection);
        vec4 mainDirectionSH = SH_evaluate_direct(mainDirection);

		float transmittance = 1.0; // no occluding GV cells
		if (m_CheckOcclusion) {
			// read and interpolate four geometry cells between me and neighbor
		/*	vec3 gvSampleDirA = mul(orientation, vec3(-0.5, -0.5, 0.5)) + vec3(0.5, 0.5, 0.5);
			vec3 gvSampleDirB = mul(orientation, vec3(-0.5, 0.5, 0.5))	+ vec3(0.5, 0.5, 0.5);
			vec3 gvSampleDirC = mul(orientation, vec3(0.5, 0.5, 0.5))	+ vec3(0.5, 0.5, 0.5);
			vec3 gvSampleDirD = mul(orientation, vec3(0.5, -0.5, 0.5))	+ vec3(0.5, 0.5, 0.5);

			vec3 gvSampeCoordA = cellCoord + gvSampleDirA * cellStep;
			vec3 gvSampeCoordB = cellCoord + gvSampleDirB * cellStep;
			vec3 gvSampeCoordC = cellCoord + gvSampleDirC * cellStep;
			vec3 gvSampeCoordD = cellCoord + gvSampleDirD * cellStep;

			vec4 gvSampleA = texture3D(m_GV, gvSampeCoordA);
			vec4 gvSampleB = texture3D(m_GV, gvSampeCoordB);
			vec4 gvSampleC = texture3D(m_GV, gvSampeCoordC);
			vec4 gvSampleD = texture3D(m_GV, gvSampeCoordD);
			vec4 gvSampleAverage = (gvSampleA + gvSampleB + gvSampleC + gvSampleD) * 0.25; // interpolate 
		*/
			vec4 gvSampleAverage = texture(m_GV, neighborCoord);
		
			transmittance = 1;//clamp(1.0 - gvSampleAverage * SH_evaluate_direct(-mainDirection)) 0.0, 1.0).x; // TODO: FIX this ???
		}

		float occludedDirectFaceContribution = transmittance * directFaceSubtendedSolidAngle;

		vec4 red   = m_FluxScale * occludedDirectFaceContribution * dot(neighbourCoeffs.red, mainDirectionSH) * mainDirectionCosineLobeSH;
		vec4 green = m_FluxScale * occludedDirectFaceContribution * dot(neighbourCoeffs.green, mainDirectionSH) * mainDirectionCosineLobeSH;
		vec4 blue  = m_FluxScale * occludedDirectFaceContribution * dot(neighbourCoeffs.blue, mainDirectionSH) * mainDirectionCosineLobeSH;

		float brightness = sqrt(1 + max(red.x, 0) + max(blue.x, 0) + max(green.x, 0));
        contribution.red += red / brightness;
        contribution.green += green / brightness;
        contribution.blue += blue / brightness;

       /* for (int sideFace = 0; sideFace < 4; sideFace++) {
            vec3 evalDirection = Prop_getEvalSideDirection(sideFace, orientation);
            vec3 reprojDirection = Prop_getReprojSideDirection(sideFace, orientation);

            float occludedSideFaceContribution = transmittance * sideFaceSubtendedSolidAngle;

            vec4 reprojDirectionCosineLobeSH = SH_evaluateCosineLobe_direct(reprojDirection);
            vec4 evalDirectionSH = SH_evaluate_direct(evalDirection);

            contribution.red += SCALE * occludedSideFaceContribution * dot(neighbourCoeffs.red, evalDirectionSH) * reprojDirectionCosineLobeSH;
            contribution.green += SCALE * occludedSideFaceContribution * dot(neighbourCoeffs.green, evalDirectionSH) * reprojDirectionCosineLobeSH;
            contribution.blue += SCALE * occludedSideFaceContribution * dot(neighbourCoeffs.blue, evalDirectionSH) * reprojDirectionCosineLobeSH;
        }*/
    }

    return contribution;
}

void main()
{
    SHcoeffs contribution = Prop_gatherContributions(lpvTexCoord);

    gl_FragData[0] = contribution.red;
    gl_FragData[1] = contribution.green;
    gl_FragData[2] = contribution.blue;
}