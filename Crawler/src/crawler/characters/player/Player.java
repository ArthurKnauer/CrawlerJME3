/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.characters.player;

import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import common.material.MaterialLoader;
import crawler.attributes.Inventory;
import crawler.characters.BipedProperties;
import crawler.characters.CustomCharacterControl;
import crawler.main.Globals;
import crawler.weapons.WeaponBuilder;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
@Builder
public class Player extends Node {

	@Getter private final CustomCharacterControl charCtrl;
	@Getter private final BipedProperties bipedProperties;
	@Getter private final PlayerBipedControl playerBipedCtrl;
	@Getter private final PlayerInputControl playerInputCtrl;
	@Getter private final EquippedItemControl equippedItemCtrl;
	@Getter private final Node eyeNode;
	@Getter private final Node weaponNode;
	@Getter private final Inventory inventory;

	public static Player build(Camera camera) {
		AssetManager assetManager = Globals.getAssetManager();
		Spatial model = assetManager.loadModel("Models/player.mesh.j3o");
		
		model.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
		
		Material material = MaterialLoader.load("Textures/player.dds");
		model.setMaterial(material);

		Node eyeNode = new Node("eyeNode");
		eyeNode.setLocalTranslation(0, 1.6f, 0);

		eyeNode.addControl(new CameraControl(camera));

		Node weaponNode = new Node("weapon");
		weaponNode.attachChild(WeaponBuilder.build("ak47", Globals.getWeaponTypeSet().get("ak47")));

		Globals.getLpvProcessor().setFlashLight(weaponNode);

		AnimControl animControl = model.getControl(AnimControl.class);
		animControl.createChannel();

		CustomCharacterControl charCtrl = new CustomCharacterControl(0.3f, 1.8f, 80);
		BipedProperties properties = new BipedProperties.Builder().setMaxMoveSpeed(4).build();
		PlayerBipedControl playerBipedCtrl = new PlayerBipedControl(charCtrl, animControl, properties, eyeNode, weaponNode);
		PlayerInputControl playerInputCtrl = new PlayerInputControl(playerBipedCtrl);
		EquippedItemControl equippedItemCtrl = new EquippedItemControl(weaponNode, eyeNode);

		Inventory inventory = new Inventory();

		Player player = Player.builder()
				.charCtrl(charCtrl)
				.bipedProperties(properties)
				.playerBipedCtrl(playerBipedCtrl)
				.playerInputCtrl(playerInputCtrl)
				.charCtrl(charCtrl)
				.equippedItemCtrl(equippedItemCtrl)
				.eyeNode(eyeNode)
				.weaponNode(weaponNode)
				.inventory(inventory).build();

		player.attachChild(model);
		player.attachChild(eyeNode);
		player.attachChild(weaponNode);

		player.addControl(playerInputCtrl);
		player.addControl(playerBipedCtrl);
		player.addControl(charCtrl);
		player.addControl(equippedItemCtrl);

		player.addAttribute(inventory);

		Globals.getPhysicsSpace().add(charCtrl);

		return player;
	}

}
