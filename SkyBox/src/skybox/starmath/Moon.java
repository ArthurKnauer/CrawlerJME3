/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybox.starmath;

import com.jme3.math.FastMath;
import static com.jme3.math.FastMath.abs;
import static com.jme3.math.FastMath.atan2;
import static com.jme3.math.FastMath.cos;
import static com.jme3.math.FastMath.sin;
import static com.jme3.math.FastMath.sqrt;
import com.jme3.math.Vector3f;
import java.util.Calendar;

/**
 * Formulae taken from "How to compute planetary positions" by By Paul Schlyter, Stockholm, Sweden
 *
 * @see <a href="http://www.stjarnhimlen.se/comp/ppcomp.html">Computing planetary positions</a>
 * @author VTPlusAKnauer
 */
class Moon {

	private float obliquity;

	Moon() {
	}

	void setEarthObliquity(float obliquity) {
		this.obliquity = obliquity;
	}

	Vector3f equatorialPosition(Calendar localTime) {
		float day = daysSince2000(localTime);

		float ascendingNodeLongitude = 125.1228f * FastMath.DEG_TO_RAD - 0.0529538083f * day * FastMath.DEG_TO_RAD;
		float inclination = 5.1454f * FastMath.DEG_TO_RAD;
		float perihelionArgument = 318.0634f * FastMath.DEG_TO_RAD + 0.1643573223f * day * FastMath.DEG_TO_RAD;
		float semiMajorAxis = 60.2666f;
		float eccentricity = 0.054900f;
		float meanAnomaly = 115.3654f * FastMath.DEG_TO_RAD + 13.0649929509f * day * FastMath.DEG_TO_RAD;

		float eccentricAnomaly = meanAnomaly + eccentricity * sin(meanAnomaly)
											   * (1.0f + eccentricity * cos(meanAnomaly));
		for (int i = 0; i < 10; i++) {

			float eccentricAnomaly1 = eccentricAnomaly
									  - (eccentricAnomaly - eccentricity * sin(eccentricAnomaly) - meanAnomaly)
										/ (1 - eccentricity * cos(eccentricAnomaly));

			if (abs(eccentricAnomaly1 - eccentricAnomaly) < 0.001f) {
				eccentricAnomaly = eccentricAnomaly1;
				break;
			}
			eccentricAnomaly = eccentricAnomaly1;
		}

		float xv = semiMajorAxis * (cos(eccentricAnomaly) - eccentricity);
		float yv = semiMajorAxis * (sqrt(1.0f - eccentricity * eccentricity) * sin(eccentricAnomaly));

		float trueAnomaly = atan2(yv, xv);
		float distance = sqrt(xv * xv + yv * yv);

		float eclipticX = distance * (cos(ascendingNodeLongitude) * cos(trueAnomaly + perihelionArgument)
									  - sin(ascendingNodeLongitude) * sin(trueAnomaly + perihelionArgument) * cos(inclination));

		float ecliptixY = distance * (sin(ascendingNodeLongitude) * cos(trueAnomaly + perihelionArgument)
									  + cos(ascendingNodeLongitude) * sin(trueAnomaly + perihelionArgument) * cos(inclination));

		float eclipticZ = distance * (sin(trueAnomaly + perihelionArgument) * sin(inclination));

		Vector3f eclipticPosition = new Vector3f(eclipticX, ecliptixY, eclipticZ).normalizeLocal();
//		float longitudeEcliptic = atan2(ecliptixY, eclipticX);
//		float latitudeEcliptic = atan2(eclipticZ, sqrt(eclipticX * eclipticX + ecliptixY * ecliptixY));
//
//		if (longitudeEcliptic < 0)
//			longitudeEcliptic += FastMath.TWO_PI;



		return CoordinateConverter.convertToEquatorial(eclipticPosition, obliquity);
	}

	private float daysSince2000(Calendar localTime) {
		int year = localTime.get(Calendar.YEAR);
		int month = localTime.get(Calendar.MONTH);
		int dayOfMonth = localTime.get(Calendar.DAY_OF_MONTH);

		int daysSince2000 = 367 * year - 7 * (year + (month + 9) / 12) / 4 + 275 * month / 9 + dayOfMonth - 730530;

		float hour = localTime.get(Calendar.HOUR_OF_DAY) + localTime.get(Calendar.MINUTE) / 60f
					 + localTime.get(Calendar.SECOND) / 3600f;

		return daysSince2000 + hour / 24.0f;
	}
}
