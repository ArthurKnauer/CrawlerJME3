package crawler.characters.npc;

import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import common.material.MaterialLoader;
import crawler.characters.BipedProperties;
import crawler.characters.CustomCharacterControl;
import crawler.characters.HealthControl;
import crawler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */
public class NPCBuilder {

	private Spatial model;
	private Spatial ragdoll;

	private NPCBuilder() {
	}

	public static NPCBuilder start() {
		return new NPCBuilder();
	}

	public NPCBuilder setModel(String modelFile) {
		AssetManager assetManager = Globals.getAssetManager();
		model = assetManager.loadModel(modelFile);
		
		model.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
		return this;
	}

	public NPCBuilder setRagdoll(String ragdollFile) {
		ragdoll = Globals.getAssetManager().loadModel(ragdollFile);
		return this;
	}

	public NPCBuilder setMaterial(String diffuseTextureFile, String normalTextureFile) {
		Material material = MaterialLoader.load(diffuseTextureFile, normalTextureFile);
		model.setMaterial(material);
		return this;
	}

	public Node build() {
		Node npc = new Node();
		Node eyeNode = new Node();
		Node weapon = new Node();
		npc.attachChild(model);
		npc.attachChild(eyeNode);
		npc.attachChild(weapon);

		AnimControl animControl = model.getControl(AnimControl.class);
		animControl.createChannel();
		
		CustomCharacterControl charControl = new CustomCharacterControl(0.3f, 1.8f, 80);
		BipedProperties properties = new BipedProperties.Builder().setMaxMoveSpeed(2f).build();
		NPCBipedControl npcBipedControl = new NPCBipedControl(charControl, animControl, properties);
		PathFollowControl pathFollowControl = new PathFollowControl(npcBipedControl);
		AIControl aiControl = new AIControl(npcBipedControl, pathFollowControl);

		// control order is important for all commands to flow thru in the same frame
		npc.addControl(aiControl);
		npc.addControl(pathFollowControl);
		npc.addControl(npcBipedControl);
		npc.addControl(charControl);
		npc.addControl(new HealthControl());

		Globals.getPhysicsSpace().add(charControl);

		return npc;
	}

}
