/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.debug;

import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author AK
 */
public class DebugPicture3DSliceControl extends AbstractControl {

	private final Material material;
	private final int textureDepth;
	private final BitmapText text;
	private final String pictureName;

	private final long cycleTimeMillis = 10000;

	public DebugPicture3DSliceControl(String pictureName, BitmapText text, Material material, int textureDepth) {
		this.material = material;
		this.textureDepth = textureDepth;
		this.pictureName = pictureName;
		this.text = text;
	}

	@Override
	protected void controlUpdate(float tpf) {
		float sliceCoord = (System.currentTimeMillis() % cycleTimeMillis) / (float) cycleTimeMillis;
		material.setFloat("SliceCoord", sliceCoord);

		text.setText(pictureName + (int) (sliceCoord * textureDepth));
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}
}
