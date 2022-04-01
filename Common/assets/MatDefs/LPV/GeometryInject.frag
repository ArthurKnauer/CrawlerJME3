uniform float m_SHScale;

in vec3 Normal_FS;
in float Area_FS;

#define SH_C0 0.282094792 // 1 / 2sqrt(pi)
#define SH_C1 0.488602512 // sqrt(3/pi) / 2

#define COS_LOBE_C0 0.88622692545276 // sqrt(pi) / 2 
#define COS_LOBE_C1 1.02332670794649 // sqrt(pi / 3)

vec4 createCosLobeSH(vec3 normal) {
    return vec4(COS_LOBE_C0, -COS_LOBE_C1 * normal.y, COS_LOBE_C1 * normal.z, -COS_LOBE_C1 * normal.x);
}

vec4 evalSH(vec3 normal) {
    return vec4(SH_C0, SH_C1 * normal.y, SH_C1 * -normal.z, SH_C1 * normal.x);
}
  
void main() {   
    vec4 sh = createCosLobeSH(Normal_FS);    
 
    gl_FragData[0] = m_SHScale * Area_FS * sh;
} 