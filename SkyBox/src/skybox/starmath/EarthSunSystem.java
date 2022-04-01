package skybox.starmath;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.Calendar;
import java.util.logging.Level;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 * Models the orientations of the sun and stars relative to an observer on Earth.
 * <p>
 * Three right-handed Cartesian coordinate systems are used: ecliptical, equatorial, and world.
 * <p>
 * In ecliptical coordinates:<ul>
 * <li>+X points to the vernal equinox
 * <li>+Y points to the celestial equator 90 degrees east of the vernal equinox
 * <li>+Z points to the north celestial pole
 * </ul>
 * In equatorial coordinates:<ul>
 * <li>+X points to the vernal equinox
 * <li>+Y points to the celestial equator 90 degrees east of the vernal equinox
 * <li>+Z points to the north celestial pole
 * </ul>
 * In world coordinates:<ul>
 * <li>+X points to the north horizon,
 * <li>+Y points to the zenith
 * <li>+Z points to the east horizon
 * </ul>
 *
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
@Log
public class EarthSunSystem {

	final private static int HOURS_PER_DAY = 24;
	final private static float RADIANS_PER_HOUR = FastMath.TWO_PI / HOURS_PER_DAY;
	final private static float DEFAULT_LATITUDE = 51.1788f * FastMath.DEG_TO_RAD;
	final private static float OBLIQUITY = 23.44f * FastMath.DEG_TO_RAD;

	private float hour = 0f;
	private float observerLatitude = DEFAULT_LATITUDE;
	private float solarLongitude = 0f;
	private float solarRightAscensionHours = 0f;

	private final Moon moon;
	@Getter private Vector3f moonDirection = new Vector3f(Vector3f.UNIT_X);

	public EarthSunSystem() {
		moon = new Moon();
		moon.setEarthObliquity(OBLIQUITY);
	}

	public Vector3f getSunDirection() {
		Vector3f result = convertToWorld(0f, solarLongitude);

		assert result.isUnitVector();
		return result;
	}

	/**
	 * Returns the orientations for a stars spatial (maybe skybox or skysphere)
	 *
	 * @return rotation quaternion
	 */
	public Quaternion getStarsRotation() {
		Quaternion yRotation = new Quaternion();
		yRotation.fromAngleNormalAxis(-getSiderealAngle(), Vector3f.UNIT_Y);

		Quaternion zRotation = new Quaternion();
		float coLatitude = FastMath.HALF_PI - observerLatitude;
		zRotation.fromAngleNormalAxis(-coLatitude, Vector3f.UNIT_Z);

		return zRotation.multLocal(yRotation);
	}

	public void setObserverLatitude(float latitude) {
		if (latitude < -FastMath.HALF_PI || latitude > FastMath.HALF_PI) {
			throw new IllegalArgumentException("latitude should be between -Pi/2 and Pi/2, inclusive");
		}

		this.observerLatitude = latitude;
	}

	/**
	 * Set the sun's celestial longitude to approximate a specified day of the year.
	 * <p>
	 * This convenience method uses a crude approximation which is accurate within a couple degrees of arc. A more
	 * accurate formula may be obtained from Steyaert, C. (1991) "Calculating the solar longitude 2000.0", WGN (Journal
	 * of the International Meteor Organization) 19-2, pages 31-34, available from
	 * http://adsabs.harvard.edu/full/1991JIMO...19...31S
	 *
	 * @param localTime
	 */
	public void setLocalTime(Calendar localTime) {
		hour = localTime.get(Calendar.HOUR_OF_DAY) + localTime.get(Calendar.MINUTE) / 60f
			   + localTime.get(Calendar.SECOND) / 3600f;

		int dayOfYear = localTime.get(Calendar.DAY_OF_YEAR);

		// Compute the approximate solar longitude (in radians).
		float daysSinceEquinox = dayOfYear - 80;
		float longitude = FastMath.TWO_PI * daysSinceEquinox / 366f;

		longitude = ((longitude % FastMath.TWO_PI) + FastMath.TWO_PI) % FastMath.TWO_PI;
		setSolarLongitude(longitude);

		Vector3f equatorial = moon.equatorialPosition(localTime);
		moonDirection = convertToWorld(equatorial);
	}

	/**
	 * Compute the angle between the meridian and the vernal equinox.
	 *
	 * @return angle (in radians, &lt;2*Pi, &ge;0)
	 */
	private float getSiderealAngle() {
		float siderealHour = getSiderealHour();
		float siderealAngle = siderealHour * RADIANS_PER_HOUR;

		assert siderealAngle >= 0f : siderealAngle;
		assert siderealAngle < FastMath.TWO_PI : siderealAngle;
		return siderealAngle;
	}

	/**
	 * Compute the sidereal time.
	 *
	 * @return time (in hours, &lt;24, &ge;0)
	 */
	private float getSiderealHour() {
		float noon = 12f;
		float siderealHour = hour - noon - solarRightAscensionHours;

		siderealHour = (siderealHour % HOURS_PER_DAY + HOURS_PER_DAY) % HOURS_PER_DAY;

		return siderealHour;
	}

	/**
	 * Convert ecliptical angles into a world direction vector.
	 *
	 * @param latitude celestial latitude (radians north of the ecliptic, &le;Pi/2, &ge;-Pi/2)
	 * @param longitude celestial longitude (radians east of the vernal equinox, &le;2*Pi, &ge;0)
	 * @return new unit vector in world (horizontal) coordinates
	 */
	private Vector3f convertToWorld(float latitude, float longitude) {
		if (!(latitude >= -FastMath.HALF_PI && latitude <= FastMath.HALF_PI)) {
			log.log(Level.SEVERE, "latitude={0}", latitude);
			throw new IllegalArgumentException(
					"latitude should be between -Pi/2 and Pi/2, inclusive");
		}
		if (!(longitude >= 0f && longitude <= FastMath.TWO_PI)) {
			log.log(Level.SEVERE, "latitude={0}", latitude);
			throw new IllegalArgumentException(
					"longitude should be between 0 and 2*Pi, inclusive");
		}

		Vector3f equatorial = CoordinateConverter.convertToEquatorial(latitude, longitude, OBLIQUITY);
		Vector3f world = convertToWorld(equatorial);

		assert world.isUnitVector();
		return world;
	}

	/**
	 * Convert equatorial coordinates to world (horizontal) coordinates.
	 *
	 * @param equatorial coordinates (not null, unaffected)
	 * @return new vector in a world coordinates
	 */
	private Vector3f convertToWorld(Vector3f equatorial) {

		float siderealAngle = getSiderealAngle();
		/*
		 * The conversion consists of a (-siderealAngle) rotation about the Z
		 * (north celestial pole) axis followed by a (latitude - Pi/2) rotation
		 * about the Y (east) axis followed by a permutation of the axes.
		 */
		Quaternion zRotation = new Quaternion();
		zRotation.fromAngleNormalAxis(-siderealAngle, Vector3f.UNIT_Z);
		Vector3f rotated = zRotation.mult(equatorial);

		float coLatitude = FastMath.HALF_PI - observerLatitude;
		Quaternion yRotation = new Quaternion();
		yRotation.fromAngleNormalAxis(-coLatitude, Vector3f.UNIT_Y);
		rotated = yRotation.mult(rotated);

		Vector3f world = new Vector3f(-rotated.x, rotated.z, rotated.y);

		return world;
	}

	/**
	 * Alter the sun's celestial longitude directly.
	 *
	 * @param longitude radians east of the vernal equinox (&le;2*Pi, &ge;0)
	 */
	private void setSolarLongitude(float longitude) {
		if (!(longitude >= 0f && longitude <= FastMath.TWO_PI)) {
			log.log(Level.SEVERE, "longitude={0}", longitude);
			throw new IllegalArgumentException(
					"longitude should be between 0 and 2*Pi");
		}

		solarLongitude = longitude;
		/*
		 * Update the cached solar right ascension.
		 */
		Vector3f equatorial = CoordinateConverter.convertToEquatorial(0f, longitude, OBLIQUITY);
		float ra = -FastMath.atan2(equatorial.y, equatorial.x);
		solarRightAscensionHours = ((ra / RADIANS_PER_HOUR) % HOURS_PER_DAY + HOURS_PER_DAY) % HOURS_PER_DAY;

		assert solarRightAscensionHours >= 0f : solarRightAscensionHours;
		assert solarRightAscensionHours < HOURS_PER_DAY : solarRightAscensionHours;
	}

	@Override
	public String toString() {
		float latitudeDegrees = observerLatitude * FastMath.RAD_TO_DEG;
		float longitudeDegrees = solarLongitude * FastMath.RAD_TO_DEG;
		String result = String.format(
				"[hour=%f, lat=%f deg, long=%f deg, ra=%f]",
				hour, latitudeDegrees, longitudeDegrees, solarRightAscensionHours);

		return result;
	}

//	public static void main(String[] ignored) {
//		log.setLevel(Level.INFO);
//		System.out.print("Test results for class EarthSunSystem:\n\n");
//
//		EarthSunSystem test = new EarthSunSystem();
//		System.out.printf("Default value:  %s%n", test.toString());
//
//		test.setSolarLongitude(Calendar.DECEMBER, 31);
//		System.out.printf(" on December 31st:  %s%n", test.toString());
//
//		test.setSolarLongitude(Calendar.JANUARY, 1);
//		System.out.printf(" on January 1st:  %s%n", test.toString());
//
//		test.setSolarLongitude(Calendar.FEBRUARY, 29);
//		System.out.printf(" on February 29th:  %s%n", test.toString());
//
//		test.setSolarLongitude(Calendar.MARCH, 1);
//		System.out.printf(" on March 1st:  %s%n", test.toString());
//	}
}
