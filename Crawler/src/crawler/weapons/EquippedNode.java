package crawler.weapons;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import crawler.util.Intersection;
import static java.lang.Math.min;
import java.util.Optional;



public class EquippedNode extends Node {
	
	private final Node eyeNode;
	private final Node shouldersNode;
	
	private final float maxTargetDist = 2.25f;
	private final float acceleration = 20.0f;
	private Vector3f oldPos = null;

	public EquippedNode(Node eyeNode, Node shouldersNode) {
		this.eyeNode = eyeNode;
		this.shouldersNode = shouldersNode;
	}	
	
	public void update(float tpf) {
//		Optional<Vector3f> position = getTargetPosition();
//
//		if (position.isPresent()) {		
//			moveTo(position.get(), tpf);		
//		}
		
//		Quaternion viewRot = eyeNode.getWorldRotation();
//		Quaternion shoulderRot = shouldersNode.getWorldRotation();
//		setLocalRotation(shoulderRot.inverse().mult(viewRot));
	}
	
	private Optional<Vector3f> getTargetPosition() {
		Vector3f viewOrigin = eyeNode.getWorldTranslation();
		Quaternion viewRot = eyeNode.getWorldRotation();
		Vector3f viewDir = viewRot.mult(new Vector3f(0, 0, 1));
		Vector3f viewUp = viewRot.mult(new Vector3f(0, 1, 0));

		Optional<Vector3f> position = Intersection.lineWithSphere(
				viewOrigin, viewDir, shouldersNode.getWorldTranslation(), 0.5f);

		if (position.isPresent()) {
			position.get().addLocal(viewUp.mult(-0.2f));
		}
		
		return position;
	}

	private void moveTo(Vector3f worldPosition, float tpf) {
		Vector3f newPosition;
		if (oldPos != null) { // accelarate towards worldPosition
			newPosition = new Vector3f(oldPos);
			Vector3f move = worldPosition.subtract(oldPos);

			float dist = move.length();
			float boost = 1;
			if (dist > maxTargetDist) {		
			//	boost = dist / maxTargetDist;
//				Vector3f maxDistMove = move.mult(1 - maxTargetDist / dist);
//				newPosition.addLocal(maxDistMove);		
//				worldPosition.subtract(newPosition, move);
			}
			
			move.multLocal(min(1, acceleration * tpf * boost));	
			newPosition.addLocal(move);
			
		} else {			
			newPosition = worldPosition;
			oldPos = new Vector3f(newPosition);
		}
		
		Vector3f localPosition = new Vector3f();
		shouldersNode.worldToLocal(newPosition, localPosition);
		setLocalTranslation(localPosition);
		
		oldPos.set(newPosition);
	}
	
}
