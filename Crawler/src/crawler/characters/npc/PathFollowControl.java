package crawler.characters.npc;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.AbstractControl;
import crawler.characters.MoveDirection;
import crawler.debug.LineList;
import crawler.main.Globals;
import static crawler.properties.CrawlerProperties.PROPERTIES;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public class PathFollowControl extends AbstractControl {
	
	private static final boolean DEBUG = PROPERTIES.getBoolean("PathFollowControl.debug");

	private final NPCBipedControl bipedControl;
	private List<Vector3f> path;
	private int currentNodeIndex;
	private final float nodeReachedMaxDistSquared = 0.025f;

	//debug	
	private LineList pathLineList;

	public PathFollowControl(NPCBipedControl bipedControl) {
		this.bipedControl = bipedControl;

		if (DEBUG) { // debug path lines
			pathLineList = new LineList("PathFollowControl_Debug", 16);
			pathLineList.setQueueBucket(RenderQueue.Bucket.Transparent);
			pathLineList.setCullHint(CullHint.Never);
			Globals.getRootNode().attachChild(pathLineList);
		}
	}

	public void moveTo(Vector3f targetPosition) {
		Vector3f location = spatial.getWorldTranslation();
		path = Globals.getSuperNavMesh().findBestPath(location, targetPosition, 0.4f);

		currentNodeIndex = 1; // first entry is current position

		if (DEBUG) {
			int n = 0;
			for (Vector3f nodePosition : path) {
				pathLineList.setNodeLocation(n++, nodePosition.add(0, 1, 0));
			}
			pathLineList.setFinalNode(n - 1);
		}
	}

	void stop() {
		path = null;
		bipedControl.move(MoveDirection.STOP);
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (path != null) {
			if (path.size() > currentNodeIndex) {
				Vector3f location = spatial.getWorldTranslation();
				Vector3f nextNode = path.get(currentNodeIndex);
				Vector3f dirToNextNode = nextNode.subtract(location).normalizeLocal();
				bipedControl.turnTo(dirToNextNode, tpf);
				MoveDirection walkDir = MoveDirection.relativeMoveDirection(bipedControl.getForwardDirection(), dirToNextNode);
				bipedControl.move(walkDir);

				if (nextNode.distanceSquared(location) < nodeReachedMaxDistSquared) {
					currentNodeIndex++;
				}
			}
			else {
				bipedControl.move(MoveDirection.STOP);
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}
}
