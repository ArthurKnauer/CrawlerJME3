/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.furniture;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import common.material.MaterialLoader;
import crawler.audio.AudioNodeEx;

/**
 *
 * @author VTPlusAKnauer
 */
public class Piano extends Node implements ActionListener {

	Camera camera;
	Node keyboard;
	public RigidBodyControl control;
	AudioNodeEx keySound[][] = new AudioNodeEx[3][7];
	Spatial whiteKey[] = new Spatial[49];

	int currentKeyPressed = 0;
	float currentKeyDepression = 0;

	private static final float KEY_RELAX_SPEED = 0.03f;
	private static final float MAX_KEY_DEPRESSION = 0.013f;

	public Piano(AssetManager assetManager, BulletAppState bulletAppState, Camera camera) {

		Spatial piano = assetManager.loadModel("Models/piano.mesh.j3o");
		Material pianoMat = MaterialLoader.load("Textures/piano.dds");
		piano.setMaterial(pianoMat);
		
		piano.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
		attachChild(piano);

		control = new RigidBodyControl(new BoxCollisionShape(new Vector3f(0.75f, 0.75f, 0.75f)), 0);
		addControl(control);
		bulletAppState.getPhysicsSpace().add(this);

		control.setPhysicsLocation(new Vector3f(-7, 0, 18));
		Quaternion pianoRot = new Quaternion();
		pianoRot.fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0));
		control.setPhysicsRotation(pianoRot);

		Geometry cube = new Geometry("cube", new Box(0.8f, 0.03f, 0.1f));
		//cube.setMaterial(pianoMat);
		cube.setCullHint(CullHint.Always);

		keyboard = new Node();
		attachChild(keyboard);
		keyboard.attachChild(cube);

		cube.setLocalTranslation(0.05f, 0.79f, 0.55f);

		keySound[0][0] = new AudioNodeEx(assetManager, "Sounds/piano/c1.wav", false);
		keySound[0][1] = new AudioNodeEx(assetManager, "Sounds/piano/d1.wav", false);
		keySound[0][2] = new AudioNodeEx(assetManager, "Sounds/piano/e1.wav", false);
		keySound[0][3] = new AudioNodeEx(assetManager, "Sounds/piano/f1.wav", false);
		keySound[0][4] = new AudioNodeEx(assetManager, "Sounds/piano/g1.wav", false);
		keySound[0][5] = new AudioNodeEx(assetManager, "Sounds/piano/a1.wav", false);
		keySound[0][6] = new AudioNodeEx(assetManager, "Sounds/piano/b1.wav", false);

		keySound[1][0] = new AudioNodeEx(assetManager, "Sounds/piano/c3.wav", false);
		keySound[1][1] = new AudioNodeEx(assetManager, "Sounds/piano/d3.wav", false);
		keySound[1][2] = new AudioNodeEx(assetManager, "Sounds/piano/e3.wav", false);
		keySound[1][3] = new AudioNodeEx(assetManager, "Sounds/piano/f3.wav", false);
		keySound[1][4] = new AudioNodeEx(assetManager, "Sounds/piano/g3.wav", false);
		keySound[1][5] = new AudioNodeEx(assetManager, "Sounds/piano/a3.wav", false);
		keySound[1][6] = new AudioNodeEx(assetManager, "Sounds/piano/b3.wav", false);

		keySound[2][0] = new AudioNodeEx(assetManager, "Sounds/piano/c5.wav", false);
		keySound[2][1] = new AudioNodeEx(assetManager, "Sounds/piano/d5.wav", false);
		keySound[2][2] = new AudioNodeEx(assetManager, "Sounds/piano/e5.wav", false);
		keySound[2][3] = new AudioNodeEx(assetManager, "Sounds/piano/f5.wav", false);
		keySound[2][4] = new AudioNodeEx(assetManager, "Sounds/piano/g5.wav", false);
		keySound[2][5] = new AudioNodeEx(assetManager, "Sounds/piano/a5.wav", false);
		keySound[2][6] = new AudioNodeEx(assetManager, "Sounds/piano/b5.wav", false);

		for (int oct = 0; oct < 3; oct++) {
			for (int note = 0; note < 7; note++) {
				keySound[oct][note].setLooping(false);
				keySound[oct][note].setPositional(false);
				keySound[oct][note].setDirectional(false);
				keySound[oct][note].setReverbEnabled(false);
				attachChild(keySound[oct][note]);
			}
		}

		this.camera = camera;

		for (int key = 0; key < 49; key++) { // create white keys
			whiteKey[key] = assetManager.loadModel("Models/piano_key_white.mesh.j3o");
			whiteKey[key].setMaterial(pianoMat);
			whiteKey[key].setShadowMode(RenderQueue.ShadowMode.Receive);
			whiteKey[key].setLocalTranslation(0.03f * key, 0, 0);

			attachChild(whiteKey[key]);
		}

		for (int octave = 0; octave < 7; octave++) { // create black keys
			for (int note = 0; note < 7; note++) { // create black keys                
				if (note != 2 && note != 6) {
					Spatial blackKey = assetManager.loadModel("Models/piano_key_black.mesh.j3o");
					blackKey.setMaterial(pianoMat);
					blackKey.setShadowMode(RenderQueue.ShadowMode.Receive);

					blackKey.setLocalTranslation((octave * 7 * 0.03f) + (note * 0.03f), 0, 0);
					attachChild(blackKey);
				}
			}
		}
	}

	public void onAction(String name, boolean isPressed, float tpf) {
		if (name.equals("use") && isPressed) {
			Ray ray = new Ray(camera.getLocation(), camera.getDirection());
			ray.intersects(Vector3f.NAN, Vector3f.NAN, Vector3f.NAN);
			CollisionResults results = new CollisionResults();
			keyboard.collideWith(ray, results);
			if (results.size() > 0) {
				CollisionResult hit = results.getCollision(0);
				Vector3f vec = hit.getContactPoint();
				Matrix4f m = new Matrix4f();
				getLocalToWorldMatrix(m);
				m.invert();
				vec = m.mult(vec);

				int idx = Math.max(0, Math.min(48, (int) (49 * (vec.x + 0.7f) / 1.47f)));
				System.out.println(idx);

				int note = idx % 7;
				int octave = idx / 7;

				float pitch = 1.0f;
				int octIdx = 0;
				switch (octave) {
					case 0: octIdx = 0;
						pitch = 0.5f;
						break;
					case 1: octIdx = 0;
						pitch = 1.0f;
						break;
					case 2: octIdx = 1;
						pitch = 0.5f;
						break;
					case 3: octIdx = 1;
						pitch = 1.0f;
						break;
					case 4: octIdx = 2;
						pitch = 0.5f;
						break;
					case 5: octIdx = 2;
						pitch = 1.0f;
						break;
					case 6: octIdx = 2;
						pitch = 2.0f;
						break;
				}

				keySound[octIdx][note].stop();
				keySound[octIdx][note].setPitch(pitch);
				keySound[octIdx][note].play();

				// release old key
				Vector3f depression = whiteKey[currentKeyPressed].getLocalTranslation();
				depression.y = 0;
				whiteKey[currentKeyPressed].setLocalTranslation(depression);

				// press down new key
				currentKeyPressed = idx;
				depression = whiteKey[currentKeyPressed].getLocalTranslation();
				depression.y = -MAX_KEY_DEPRESSION;
				whiteKey[currentKeyPressed].setLocalTranslation(depression);
				currentKeyDepression = MAX_KEY_DEPRESSION;
			}
		}
	}

	public void update(float tpf) {
		if (currentKeyDepression > 0) {
			Vector3f depression = whiteKey[currentKeyPressed].getLocalTranslation();
			depression.y = -currentKeyDepression;
			whiteKey[currentKeyPressed].setLocalTranslation(depression);

			currentKeyDepression -= KEY_RELAX_SPEED * tpf;
			if (currentKeyDepression < 0) currentKeyDepression = 0;
		}
	}
}
