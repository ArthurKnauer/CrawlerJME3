package crawler.interiordesign;

import architect.room.Room;
import architect.walls.Opening;
import architect.walls.WallSide;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import common.material.MaterialLoader;
import crawler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */


public class WindowInstaller {
	
	public static Node buildNodeWithWindows(Room room) {
		Node roomWindows = new Node();
		float ww = 1.5f;
		for (WallSide wallSide : room.getWallSides()) {
			for (Opening opening : wallSide.getOpenings().values()) {
				if (opening.type == Opening.Type.WINDOW) {
					for (float w = 0; w < opening.width - ww * 0.5f; w += ww) {
						Node window = createWindow();
						float wpos = opening.start + wallSide.min + w + ww * 0.5f;

						Vector3f pos;
						if (wallSide.isHorizontal())
							pos = new Vector3f(wpos, opening.bottom, wallSide.pos);
						else
							pos = new Vector3f(wallSide.pos, opening.bottom, wpos);

						window.setLocalTranslation(pos);
						Vector3f wallNormal = new Vector3f(wallSide.getNormal().x, 0, wallSide.getNormal().y);
						window.lookAt(pos.add(wallNormal), Vector3f.UNIT_Y);
						window.rotate(0, FastMath.PI * 0.5f, 0);
						roomWindows.attachChild(window);
						window.setCullHint(Spatial.CullHint.Never);
					}
				}
			}
		}
		
		return roomWindows;
	}
	
	private static Node createWindow() {
		AssetManager assetManager = Globals.getAssetManager();
		
		Texture paneTex = assetManager.loadTexture(new TextureKey("Textures/window_pane.dds", false));
		Material paneMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		paneMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
		paneMat.setTexture("ColorMap", paneTex);
		
		Material windowMat = MaterialLoader.load("Textures/window_frame.dds");

		Node windowNode = new Node();
		Spatial windowFrame = assetManager.loadModel("Models/window_frame.mesh.j3o");
		windowFrame.setMaterial(windowMat);
		windowFrame.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
		windowNode.attachChild(windowFrame);

		Spatial windowPane = assetManager.loadModel("Models/window_pane.mesh.j3o");
		windowPane.setMaterial(paneMat);
		windowPane.setQueueBucket(RenderQueue.Bucket.Transparent);
		windowPane.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
		windowNode.attachChild(windowPane);

		return windowNode;
	}
	
}
