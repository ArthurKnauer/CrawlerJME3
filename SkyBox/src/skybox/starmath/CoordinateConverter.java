package skybox.starmath;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.logging.Level;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

/**
 *
 * @author VTPlusAKnauer
 */
@Log @UtilityClass
class CoordinateConverter {

	/**
	 * Convert ecliptical angles into an equatorial direction vector.
	 *
	 * @param latitude celestial latitude (radians north of the ecliptic, &le;Pi/2, &ge;-Pi/2)
	 * @param longitude celestial longitude (radians east of the vernal equinox, &le;2*Pi, &ge;0)
	 * @return new unit vector in equatorial coordinates
	 */
	static Vector3f convertToEquatorial(float latitude, float longitude, float obliquity) {
		if (!(latitude >= -FastMath.HALF_PI && latitude <= FastMath.HALF_PI)) {
			log.log(Level.SEVERE, "latitude={0}", latitude);
			throw new IllegalArgumentException("latitude should be between -Pi/2 and Pi/2, inclusive");
		}
		if (!(longitude >= 0f && longitude <= FastMath.TWO_PI)) {
			log.log(Level.SEVERE, "longitude={0}", longitude);
			throw new IllegalArgumentException("longitude should be between 0 and 2*Pi, inclusive");
		}
		/*
		 * Convert angles to Cartesian ecliptical coordinates.
		 */
		float cosLat = FastMath.cos(latitude);
		float sinLat = FastMath.sin(latitude);
		float cosLon = FastMath.cos(longitude);
		float sinLon = FastMath.sin(longitude);
		Vector3f ecliptical = new Vector3f(cosLat * cosLon, cosLat * sinLon, sinLat);
		assert ecliptical.isUnitVector();
		/*
		 * Convert to equatorial coordinates.
		 */
		Vector3f equatorial = convertToEquatorial(ecliptical, obliquity);

		assert equatorial.isUnitVector();
		return equatorial;
	}

	/**
	 * Convert ecliptical coordinates to equatorial coordinates.
	 *
	 * @param ecliptical coordinates (not null, unaffected)
	 * @return new vector in equatorial coordinates
	 */
	public static Vector3f convertToEquatorial(Vector3f ecliptical, float obliquity) {
		/*
		 * The conversion consists of a rotation about the +X
		 * (vernal equinox) axis.
		 */
		Quaternion rotate = new Quaternion();
		rotate.fromAngleNormalAxis(obliquity, Vector3f.UNIT_X);
		Vector3f equatorial = rotate.mult(ecliptical);

		return equatorial;
	}

}
