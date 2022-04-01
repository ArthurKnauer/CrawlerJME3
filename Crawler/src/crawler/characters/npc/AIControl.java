package crawler.characters.npc;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import crawler.characters.BipedControl;
import crawler.characters.ai.jbt.BTLibrary;
import crawler.characters.player.Player;
import crawler.main.Globals;
import java.util.List;
import jbt.execution.core.*;
import jbt.model.core.ModelTask;

/**
 *
 * @author VTPlusAKnauer
 */
public class AIControl extends AbstractControl {

	private final BipedControl biped;
	private final PathFollowControl pathFollowControl;

	private final IBTExecutor btExecutor;

	private boolean isMoving;

	public AIControl(BipedControl biped, PathFollowControl pathFollowControl) {
		this.biped = biped;
		this.pathFollowControl = pathFollowControl;

		IBTLibrary btLibrary = new BTLibrary();
		IContext context = ContextFactory.createContext(btLibrary);
		context.setVariable("AIControl", this);
		ModelTask tree = btLibrary.getBT("Roam");
		btExecutor = BTExecutorFactory.createBTExecutor(tree, context);
	}

	public boolean canSee(String target) {
		Vector3f playerLocation = Globals.getPlayer().getWorldTranslation();
		Vector3f playerTorsoPos = playerLocation.add(0, 1.5f, 0);
		Vector3f viewLineStart = spatial.getWorldTranslation().add(0, 2, 0);

		List<PhysicsRayTestResult> hitResults = Globals.getPhysicsSpace().rayTest(viewLineStart, playerTorsoPos);
		if (hitResults.size() > 0) {
			PhysicsCollisionObject hitObject = hitResults.get(0).getCollisionObject();
			Object user = hitObject.getUserObject();
			if (user instanceof Player) {
				return true;
			}
		}

		return false;
	}

	public void advance(String target) {
		Vector3f advancePos = Globals.getPlayer().getWorldTranslation().add(spatial.getWorldTranslation()).multLocal(0.5f);
		moveTo(advancePos);
	}

	public boolean isAdvanced(String target) {
		Vector3f dist = Globals.getPlayer().getWorldTranslation().subtract(spatial.getWorldTranslation());
		return dist.length() < 3;
	}

	public void moveTo(Vector3f targetPosition) {
		pathFollowControl.moveTo(targetPosition);
		isMoving = true;
	}

	public void stop() {
		pathFollowControl.stop();
		isMoving = false;
	}

	public boolean isMoving() {
		return isMoving;
	}

	@Override
	protected void controlUpdate(float tpf) {
		btExecutor.tick();

		isMoving = biped.actualVelocity() > 0.1f;

//		float time = (System.currentTimeMillis() % 62831853) * 0.001f + (FastMath.nextRandomFloat() - 0.5f) * 0.5f;
//		float sin = FastMath.sin(time);
//		biped.turn(sin * 4.0f * tpf, 0);
//		biped.moveTo(BipedControl.GoDir.FORWARD);
//			Vector3f location = getWorldTranslation();
//
//			Vector3f playerLocation = Globals.getPlayer().getWorldTranslation();
//			Vector3f playerTorsoPos = playerLocation.add(0, 1.5f, 0);
//			Vector3f viewLineStart = getWorldTranslation().add(0, 2, 0);
//			Vector3f viewDir = playerTorsoPos.subtract(viewLineStart).normalizeLocal();
//			viewLineStart.addLocal(viewDir);
//
//			boolean seePlayer = false;
//			List<PhysicsRayTestResult> hitResults = Globals.getPhysicsSpace().rayTest(viewLineStart, playerTorsoPos);
//			if (hitResults.size() > 0) {
//				PhysicsCollisionObject hitObject = hitResults.get(0).getCollisionObject();
//				Object user = hitObject.getUserObject();
//				if (user instanceof Player) {
//					seePlayer = true;
//				}
//			}
//
//			if (seePlayer) {
//				weaponControl.lookAt(playerTorsoPos, Vector3f.UNIT_Y);
//				attack();
//			//	goDir = GoDir.STAND;
//			}
//			else {
//			//	goDir = GoDir.FORWARD;
//			}
//
//			for (Line line : lines) {
//				line.updatePoints(Vector3f.ZERO, Vector3f.ZERO);
//			}
//
//			List<Vector3f> path = Globals.getSuperNavMesh().findBestPath(location, playerLocation, 0.4f);
//
//			if (!path.isEmpty()) {
//				for (int p = 0; p < 3 && p < path.size() - 1; p++) {
//					lines[p].updatePoints(path.get(p).add(0, 1, 0), path.get(p + 1).add(0, 1, 0));
//				}
//
//				Vector3f dir = path.get(1).subtract(location).normalizeLocal();
//				float cosAngle = getViewDirection().x * dir.x + getViewDirection().z * dir.z;
//				if (cosAngle > 1)
//					cosAngle = 1;
//				else if (cosAngle < -1)
//					cosAngle = -1;
//				float angle = (float) Math.acos(cosAngle);
//				float rot = Math.min(3.5f * tpf, angle);
//				if (getViewDirection().cross(dir).y < 0)
//					rot *= -1;
//
//				//xAngle += rot;
//			}
//
//			//else xAngle += tpf * 0.1f;
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	public void attack(String target) {
		stop();
		biped.attack();
	}

}
