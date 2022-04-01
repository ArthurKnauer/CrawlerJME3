/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.math.Vector3f;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class LPVShape {

	@Getter private final Vector3f minPos = new Vector3f();
	@Getter private final Vector3f scale = new Vector3f();
	@Getter private final Vector3f cellSize = new Vector3f();
	@Getter private final Vector3f gvMinPos = new Vector3f();

	@Getter private final int textureSizeX;
	@Getter private final int textureSizeY;
	@Getter private final int textureSizeZ;

	public LPVShape(int textureSizeX, int textureSizeY, int textureSizeZ) {
		this.textureSizeX = textureSizeX;
		this.textureSizeY = textureSizeY;
		this.textureSizeZ = textureSizeZ;
	}

	public void setMinPosAndScale(Vector3f minPos, Vector3f scale) {
		this.minPos.set(minPos);
		this.scale.set(scale);

		// normalize the scale so that lpv texels are cubes
		scale.z = Math.max(scale.z, (scale.y * textureSizeZ) / textureSizeY);
		if (scale.x < scale.z)
			scale.x = scale.z;
		else if (scale.z < scale.x)
			scale.z = scale.x;
		scale.y = (scale.x * textureSizeY) / textureSizeX;

		// scale is maybe bigger than scale, move minpos so that the center
		Vector3f scaleDiff = scale.subtract(scale).multLocal(0.5f);
		minPos.subtractLocal(scaleDiff);

		cellSize.set(2.0f / textureSizeX, 2.0f / textureSizeY, 2.0f / textureSizeZ);

		// GV is offset with -0.5 * cellSize, so that the GV cell centers are on the corners of the LPV cells
		//Vector3f cellSize = lpvCellSize.mult(scale).multLocal(0.5f);
		//gvMinPos = lpvMinPos.subtract(cellSize.mult(0.5f));
		gvMinPos.set(minPos);
	}
}
