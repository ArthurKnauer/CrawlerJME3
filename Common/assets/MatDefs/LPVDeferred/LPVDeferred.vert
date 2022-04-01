attribute vec3 inPosition;

// frustum corner rays in world space (normalized in the depth direction, which is the z axis in eye space)
uniform vec3 m_FrustumLowerLeftRay;
uniform vec3 m_FrustumLowerRightRay;
uniform vec3 m_FrustumUpperRightRay;
uniform vec3 m_FrustumUpperLeftRay;

varying vec2 texCoord;
varying vec3 viewRay;

void main(){
    texCoord = inPosition.xy;  

	viewRay = (m_FrustumLowerLeftRay * (1 - texCoord.y) + m_FrustumUpperLeftRay * texCoord.y) * (1 - texCoord.x)
		+ (m_FrustumLowerRightRay * (1 - texCoord.y) + m_FrustumUpperRightRay * texCoord.y) * (texCoord.x);

    gl_Position = vec4(inPosition * 2.0 - 1.0, 1.0);
}