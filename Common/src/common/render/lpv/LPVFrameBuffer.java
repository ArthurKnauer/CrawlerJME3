package common.render.lpv;

import com.jme3.material.Material;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Texture3D;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public final class LPVFrameBuffer {

	private final FrameBuffer[] lpvFB = new FrameBuffer[2]; // 2 ping pong buffers (light propagates from one LPV to the other, back and forth)
	private final Texture3D[][] lpvMap = new Texture3D[2][3]; // 2 ping pong LPV volumes (each with 3 colors, RGB)

	@Getter private final FrameBuffer gvFrameBuffer;	// geometry volume frame buffer
	@Getter private final Texture3D gvMap;	// geometry volume for occlusion and light bouncing

	public LPVFrameBuffer(LPVShape lpvSetup) {		
		int sizeX = lpvSetup.getTextureSizeX();
		int sizeY = lpvSetup.getTextureSizeY();
		int sizeZ = lpvSetup.getTextureSizeZ();
		
		// create Light Propagation Volume 3D textures and two framebuffers
		for (int buffer = 0; buffer < 2; buffer++) {
			lpvFB[buffer] = new FrameBuffer(sizeX, sizeY, 1);
			lpvFB[buffer].setMultiTarget(true);
			for (int color = 0; color < 3; color++) {
				lpvMap[buffer][color] = new Texture3D(sizeX, sizeY, sizeZ, Image.Format.RGBA16F);
				lpvFB[buffer].addColorTexture(lpvMap[buffer][color]);
			}
		}
		setFilterNearest();

		// create Geometry Volume texture and a framebuffer for it
		gvFrameBuffer = new FrameBuffer(sizeX, sizeY, 1);
		gvMap = new Texture3D(sizeX, sizeY, sizeZ, Image.Format.RGBA16F);
		gvMap.setMagFilter(Texture.MagFilter.Bilinear);
		gvMap.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
		gvFrameBuffer.setColorTexture(gvMap);
	}
	
	int getWidth() {
		return lpvFB[0].getWidth();
	}
	
	int getHeight() {
		return lpvFB[0].getHeight();
	}
	
	Texture3D getLPVMap(int bufferIdx, int colorIdx) {
		return lpvMap[bufferIdx][colorIdx];
	}

	public FrameBuffer getBuffer(int index) {
		return lpvFB[index];
	}
	
	public void setLPVTextures(Material shader, int index) {
		shader.setTexture("LPVRed", lpvMap[index][0]);
		shader.setTexture("LPVGreen", lpvMap[index][1]);
		shader.setTexture("LPVBlue", lpvMap[index][2]);
	}

	void setFilterNearest() {
		lpvMap[0][0].setMagFilter(Texture.MagFilter.Nearest);
		lpvMap[0][0].setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		lpvMap[0][1].setMagFilter(Texture.MagFilter.Nearest);
		lpvMap[0][1].setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		lpvMap[0][2].setMagFilter(Texture.MagFilter.Nearest);
		lpvMap[0][2].setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		
		lpvMap[1][0].setMagFilter(Texture.MagFilter.Nearest);
		lpvMap[1][0].setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		lpvMap[1][1].setMagFilter(Texture.MagFilter.Nearest);
		lpvMap[1][1].setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		lpvMap[1][2].setMagFilter(Texture.MagFilter.Nearest);
		lpvMap[1][2].setMinFilter(Texture.MinFilter.NearestNoMipMaps);
	}

	void setFilterBilinear() {
		lpvMap[0][0].setMagFilter(Texture.MagFilter.Bilinear);
		lpvMap[0][0].setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
		lpvMap[0][1].setMagFilter(Texture.MagFilter.Bilinear);
		lpvMap[0][1].setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
		lpvMap[0][2].setMagFilter(Texture.MagFilter.Bilinear);
		lpvMap[0][2].setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

		lpvMap[1][0].setMagFilter(Texture.MagFilter.Bilinear);
		lpvMap[1][0].setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
		lpvMap[1][1].setMagFilter(Texture.MagFilter.Bilinear);
		lpvMap[1][1].setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
		lpvMap[1][2].setMagFilter(Texture.MagFilter.Bilinear);
		lpvMap[1][2].setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
	}
}
