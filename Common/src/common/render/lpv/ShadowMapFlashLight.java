/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
final class ShadowMapFlashLight extends ShadowMapLight {

	@Getter private final Vector3f position = new Vector3f();
	@Getter private final Texture texture;

	ShadowMapFlashLight(AssetManager assetManager, int width, int height) {
		super(width, height);
		
		shadowCamera.setFrustumPerspective(100, 1, 1f, 50f);
		shadowCamera.setParallelProjection(false);

		texture = assetManager.loadTexture(new TextureKey("Textures/flashlight.dds", false));
		texture.setWrap(Texture.WrapMode.Clamp);
	}

	void positionAtNode(Node flashLightNode) {
		shadowCamera.setFrame(flashLightNode.getWorldTranslation(), flashLightNode.getWorldRotation());
		shadowCamera.update();
		shadowCamera.updateViewProjection();
		direction.set(shadowCamera.getDirection());
		position.set(shadowCamera.getLocation());
		
		updateLightViewProjectionMatrix();
	}
}
