uniform vec3 m_Color;

varying vec3 normal;
varying vec3 position;
varying vec2 depth;

layout (location = 0) out vec4 dffuseOut;  
layout (location = 1) out vec4 normalOut;

void main() {  
     #ifndef COLOR
        // eye-catchy simple checker shader for missing material (use of default shader)
        const float checkerScale = 0.5;  
        dffuseOut = vec4(   mod(position.x, checkerScale) < checkerScale / 2 ? 0 : 1, 
                            mod(position.y, checkerScale) < checkerScale / 2 ? 0 : 1, 
                            mod(position.z, checkerScale) < checkerScale / 2 ? 0 : 1,  1);
        dffuseOut = dffuseOut * dffuseOut; // more contrast
    #else
        dffuseOut = vec4(m_Color, 1);
    #endif


    normalOut = vec4((normal + 1) * 0.5, 1);

	gl_FragDepth = depth.x / depth.y; // z / w
}