package crawler.items;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import common.material.MaterialLoader;
import crawler.attributes.PickupAttribute;
import crawler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */
public class ItemBuilder {
	
	public static Spatial buildAndPlace(Vector3f location) {
		Spatial item = build();
		item.getControl(RigidBodyControl.class).setPhysicsLocation(location);
		item.setLocalTranslation(location);
		return item;
	}

	public static Spatial build() {
		AssetManager assetManager = Globals.getAssetManager();
		Spatial spatial = assetManager.loadModel("Models/furniture/bottle_beer.mesh.j3o");
		Material material = MaterialLoader.load("Textures/bottle_beer.dds");
		
		spatial.setMaterial(material);
		spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

		BoxCollisionShape boxCollisionShape = new BoxCollisionShape(new Vector3f(0.2f, 0.2f, 0.2f));
		RigidBodyControl rbc = new RigidBodyControl(boxCollisionShape, 1);
		rbc.setSleepingThresholds(20, 20);
		spatial.addControl(rbc);
		Globals.getPhysicsSpace().add(spatial);		

		spatial.addAttribute(new PickupAttribute());

		return spatial;
	}

}
