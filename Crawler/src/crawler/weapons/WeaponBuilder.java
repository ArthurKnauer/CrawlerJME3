package crawler.weapons;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import common.material.MaterialLoader;
import crawler.audio.AudioNodeEx;
import crawler.effects.MuzzleFlashBuilder;
import crawler.effects.MuzzleFlashControl;
import crawler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */
public class WeaponBuilder {

	public static Node build(String name, WeaponType type) {
		AssetManager assetManager = Globals.getAssetManager();

		Node node = new Node(name);
		if (type.getModelFile().isPresent()) {
			String modelFile = type.getModelFile().get();
			String textureFile = type.getTextureFile().get();
			
			Spatial weaponModel = assetManager.loadModel(modelFile);
			Material material = MaterialLoader.load(textureFile);
			
			weaponModel.setMaterial(material);
			weaponModel.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
			node.attachChild(weaponModel);
		}		
		
		Spatial muzzleFlash = MuzzleFlashBuilder.build("pistolFlash", type.getMuzzleFlashColor());
		muzzleFlash.setLocalTranslation(type.getMuzzlePos());
		node.attachChild(muzzleFlash);
		MuzzleFlashControl muzzleFlashControl = muzzleFlash.getControl(MuzzleFlashControl.class);

		AudioNodeEx fireSound = new AudioNodeEx(assetManager, type.getFireSoundFile(), false);
		fireSound.setLooping(false);
		fireSound.setPositional(true);
		fireSound.setDirectional(false);
		fireSound.setReverbEnabled(false);
		fireSound.setVolume(1);
		fireSound.setPitch(1);
		node.attachChild(fireSound);
		
		node.addControl(new WeaponControl(type, muzzleFlashControl, fireSound));
		return node;
	}
}
