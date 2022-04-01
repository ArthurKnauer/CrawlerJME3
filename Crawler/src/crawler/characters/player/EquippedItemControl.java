package crawler.characters.player;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author VTPlusAKnauer
 */
public class EquippedItemControl extends AbstractControl {

	private final Node item;
	private final Node eye;

	EquippedItemControl(Node item, Node eye) {
		this.item = item;
		this.eye = eye;
	}

	@Override
	protected void controlUpdate(float tpf) {
		Vector3f dir = new Vector3f(0, 0, 0.5f);
		Vector3f up = new Vector3f(0, -0.125f, 0);
		Quaternion eyeRot = eye.getLocalRotation();
		eyeRot.multLocal(dir);
		eyeRot.multLocal(up);
		
		Vector3f location = eye.getLocalTranslation().add(dir).addLocal(up);
		
		item.setLocalTranslation(location);
		item.setLocalRotation(eyeRot);
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}
}
