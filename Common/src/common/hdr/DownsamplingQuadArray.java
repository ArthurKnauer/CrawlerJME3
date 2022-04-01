/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.hdr;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

/**
 *
 * @author VTPlusAKnauer
 */
public class DownsamplingQuadArray extends Geometry {

	private static final int SPLITS = 4;
	public static final int QAUDS = SPLITS * SPLITS;
	private static final int TRIANGLES = QAUDS * 2;
	private static final float TEXIE_SIZE = 1.0f / SPLITS;

	public DownsamplingQuadArray() {
		super("downsamplingQuadArray", createMesh());
	}

	private static Mesh createMesh() {
		float[] vertices = new float[QAUDS * 4 * 3]; // 4 corners, 3 members x,y,z
		float[] texies = new float[QAUDS * 4 * 2]; // 4 corners, 2 members u,v
		short[] indices = new short[TRIANGLES * 3];

		for (int x = 0; x < SPLITS; x++) {
			float uOffset = x * TEXIE_SIZE;

			for (int y = 0; y < SPLITS; y++) {
				int quad = x * SPLITS + y;
				float vOffset = y * TEXIE_SIZE;

				vertices[quad * 12 + 0] = -1;
				vertices[quad * 12 + 1] = -1;
				vertices[quad * 12 + 2] = 0;

				vertices[quad * 12 + 3] = +1;
				vertices[quad * 12 + 4] = -1;
				vertices[quad * 12 + 5] = 0;

				vertices[quad * 12 + 6] = +1;
				vertices[quad * 12 + 7] = +1;
				vertices[quad * 12 + 8] = 0;

				vertices[quad * 12 + 9] = -1;
				vertices[quad * 12 + 10] = +1;
				vertices[quad * 12 + 11] = 0;

				texies[quad * 8 + 0] = uOffset;
				texies[quad * 8 + 1] = vOffset;

				texies[quad * 8 + 2] = uOffset + TEXIE_SIZE;
				texies[quad * 8 + 3] = vOffset;

				texies[quad * 8 + 4] = uOffset + TEXIE_SIZE;
				texies[quad * 8 + 5] = vOffset + TEXIE_SIZE;

				texies[quad * 8 + 6] = uOffset;
				texies[quad * 8 + 7] = vOffset + TEXIE_SIZE;

				indices[quad * 6 + 0] = (short) (quad * 4 + 0);
				indices[quad * 6 + 1] = (short) (quad * 4 + 1);
				indices[quad * 6 + 2] = (short) (quad * 4 + 2);

				indices[quad * 6 + 3] = (short) (quad * 4 + 2);
				indices[quad * 6 + 4] = (short) (quad * 4 + 3);
				indices[quad * 6 + 5] = (short) (quad * 4 + 0);
			}
		}

		Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Triangles);
		mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
		mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texies);
		mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
		
		return mesh;
	}
}
