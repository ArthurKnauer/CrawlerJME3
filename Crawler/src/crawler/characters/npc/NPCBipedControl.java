package crawler.characters.npc;

import com.jme3.animation.AnimControl;
import com.jme3.math.Vector3f;
import crawler.characters.BipedControl;
import crawler.characters.BipedProperties;
import crawler.characters.CustomCharacterControl;

/**
 *
 * @author VTPlusAKnauer
 */
public class NPCBipedControl extends BipedControl {

	public NPCBipedControl(CustomCharacterControl charControl,
						   AnimControl animControl,
						   BipedProperties properties) {
		super(charControl, animControl, properties);
	}

	public void turnTo(Vector3f direction, float tpf) {
		Vector3f forwardDir = charControl.getViewDirection();
		float angle = -forwardDir.signedAngleOnYPlane(direction);

		float xRotate;
		if (angle < 0)
			xRotate = Math.max(-properties.getMaxRotateSpeed() * tpf, angle);
		else
			xRotate = Math.min(properties.getMaxRotateSpeed() * tpf, angle);

		xAngle += xRotate;
		limitXAngleTo2PiRange();
	}

}
