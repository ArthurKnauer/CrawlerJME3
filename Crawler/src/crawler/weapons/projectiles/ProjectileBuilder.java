package crawler.weapons.projectiles;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import crawler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */


public class ProjectileBuilder {

	public static Spatial build(Vector3f location, Quaternion rotation, Vector3f velocity) {
		AssetManager assetManager = Globals.getAssetManager();
		Spatial spatial = assetManager.loadModel("Models/effects/muzzleflash_pistol.mesh.j3o");
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		material.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/effects/muzzleflash_pistol.dds", false)));
		spatial.setQueueBucket(RenderQueue.Bucket.Transparent);
		material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Additive);
		material.getAdditionalRenderState().setDepthWrite(false);
		spatial.setMaterial(material);
			
		spatial.setLocalTranslation(location);
		spatial.setLocalRotation(rotation);
		
		Globals.getRootNode().attachChild(spatial);
		spatial.addControl(new ProjectileMovement(velocity));
		
		return spatial;
	}

}
