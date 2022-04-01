/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.debug;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.texture.Texture3D;
import com.jme3.ui.Picture;

/**
 *
 * @author VTPlusAKnauer
 */
public class DebugPicture extends Picture {

	private final BitmapText text;

	public DebugPicture(Node guiNode, BitmapFont font, String name) {
		super(name);

		text = new BitmapText(font, false);
		text.setSize(font.getCharSet().getRenderedSize());
		text.setColor(ColorRGBA.White);
		text.setLocalScale(1);
		text.setText(name);

		guiNode.attachChild(text);
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
	}

	public void setTextPosition(float x, float y) {
		text.setLocalTranslation(x, y, 0);
	}

	/**
	 * Set the texture to put on the picture.
	 *
	 * @param assetManager The {@link AssetManager} to use to load the material.
	 * @param tex The texture
	 */
	public void setTexture3D(AssetManager assetManager, Texture3D tex) {
		if (getMaterial() != null) {
			throw new IllegalStateException("Texture3D can only be set once");
		}

		Material mat = new Material(assetManager, "MatDefs/Gui/3DSlice.j3md");
		setMaterial(mat);
		material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Off);
		material.setTexture("Texture", tex);

		addControl(new DebugPicture3DSliceControl(name, text, material, tex.getImage().getDepth()));
	}

}
