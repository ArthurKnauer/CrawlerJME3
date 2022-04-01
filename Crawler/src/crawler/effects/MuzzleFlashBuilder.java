package crawler.effects;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import crawler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */
public class MuzzleFlashBuilder {


	public static Spatial build(String name, ColorRGBA color) {
		AssetManager assetManager = Globals.getAssetManager();

		Spatial spatial = assetManager.loadModel("Models/effects/muzzleflash_pistol.mesh.j3o");
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		material.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/effects/muzzleflash_pistol.dds", false)));
		spatial.setQueueBucket(RenderQueue.Bucket.Transparent);
		material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Additive);
		material.getAdditionalRenderState().setDepthWrite(false);

		ColorRGBA currentColor = new ColorRGBA(0, 0, 0, 0);
		material.setColor("Color", currentColor); // invisible because of RenderState.BlendMode.Additive
		spatial.setMaterial(material);
		
		spatial.addControl(new MuzzleFlashControl(currentColor, color));
		
		return spatial;
	}

}
