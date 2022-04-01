/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.main.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.texture.Texture;
import crawler.audio.AudioNodeEx;
import crawler.main.CrawlerApp;

/**
 *
 * @author VTPlusAKnauer
 */
public class ChopperState extends AbstractAppState {

	private CrawlerApp app;
	private Node chopper;
	private Spatial rotor;
	private AudioNodeEx chopperSound;
	private AudioNodeEx boostSound;
	private AudioNodeEx startSound;

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		this.app = (CrawlerApp) app;

		chopper = (Node) app.getAssetManager().loadModel("Models/chopper.mesh.j3o");
		Material material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		Texture diffuseTexture = app.getAssetManager().loadTexture(new TextureKey("Textures/chopper.dds", false));
		material.setTexture("ColorMap", diffuseTexture);

		chopper.setMaterial(material);
		chopper.setQueueBucket(RenderQueue.Bucket.Sky3D);

		rotor = app.getAssetManager().loadModel("Models/chopper_rotor.mesh.j3o");
		rotor.setMaterial(material);
		rotor.setQueueBucket(RenderQueue.Bucket.Sky3D);
		rotor.setName("rotor");
		chopper.attachChild(rotor);

		chopperSound = new AudioNodeEx(app.getAssetManager(), "Sounds/chopper_loop.wav", false);
		chopperSound.setLooping(true);
		chopperSound.setPositional(false);
		chopper.attachChild(chopperSound);

		boostSound = new AudioNodeEx(app.getAssetManager(), "Sounds/chopper_boost_loop.wav", false);
		boostSound.setLooping(true);
		boostSound.setVolume(0);
		boostSound.setPositional(false);
		chopper.attachChild(boostSound);

		startSound = new AudioNodeEx(app.getAssetManager(), "Sounds/chopper_start.wav", false);
		startSound.setLooping(false);
		startSound.setVolume(0.1f);
		startSound.setPositional(false);
		chopper.attachChild(startSound);

		this.app.getRootNode().attachChild(chopper);

		chopper.addControl(new AbstractControl() {
			private float rotorAngle = 0;

			@Override
			protected void controlUpdate(float tpf) {
				Vector3f pos = spatial.getLocalTranslation();
				Quaternion rot = spatial.getLocalRotation();

				Vector3f camPos = app.getCamera().getDirection().mult(20)
						.addLocal(app.getCamera().getLocation())
						.addLocal(app.getCamera().getUp().mult(-5.0f));
				Quaternion camRot = app.getCamera().getRotation();

				float distToMove = pos.distance(camPos) / 2;

				float rotorPitch = 1 + rot.angleBetween(camRot) + FastMath.abs(app.getCamera().getDirection().y) + distToMove;

				boostSound.setVolume(FastMath.clamp(distToMove * 0.05f, 0, 1f));

				rotorAngle += tpf * rotorPitch * 20;
				Quaternion rotorRot = new Quaternion();
				rotorRot.fromAngleNormalAxis(rotorAngle, Vector3f.UNIT_Y);
				rotor.setLocalRotation(rotorRot);

				rotorPitch = FastMath.clamp(rotorPitch, 0.5f, 2.0f);
				chopperSound.setPitch(rotorPitch);

				pos.interpolate(camPos, 0.5f);
				rot.slerp(camRot, 0.125f);

				spatial.setLocalTranslation(pos);
				spatial.setLocalRotation(rot);
			}

			@Override
			protected void controlRender(RenderManager rm, ViewPort vp) {
			}
		});
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (enabled) {
			//app.getRootNode().attachChild(chopper);
			chopperSound.play();
			startSound.play();
			boostSound.play();
		}
		else {
			//	app.getRootNode().detachChild(chopper);
			//chopper.removeControl(chopper.getControl(0));
			chopperSound.stop();
			startSound.stop();
			boostSound.stop();
		}
	}
}
