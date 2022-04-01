package cityplanner.noise;

import com.jme3.math.Vector2f;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public class PopulationMap {

	private final float[] noise;

	private final int size = 64;
	private float sizeInMeters = 1000;

	public PopulationMap(Random random) {
		noise = new float[size * size];
		generateMap(random);
	}

	public void setSizeInMeters(float sizeInMeters) {
		this.sizeInMeters = sizeInMeters;
	}

	public float getPixelSize() {
		return sizeInMeters / size;
	}

	public void setValue(Vector2f samplePos) {
		int x = (int) ((samplePos.x / sizeInMeters + 0.5f) * size);
		int y = (int) ((0.5f - samplePos.y / sizeInMeters) * size);

		int index = x + y * size;
		if (index < 0 || index >= noise.length)
			return;
		
		noise[index] = 1;
	}

	public float getValue(Vector2f samplePos) {
		int x = (int) ((samplePos.x / sizeInMeters + 0.5f) * size);
		int y = (int) ((0.5f - samplePos.y / sizeInMeters) * size);

		int index = x + y * size;
		if (index < 0 || index >= noise.length)
			return 0;
		return noise[index];
	}

	public Texture createTexture() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(size * size * 4);

		for (int i = 0; i < noise.length; i++) {
			int y = i / size;
			int x = i % size;

//			if (x == 0) {
//				buffer.put((byte) 255);
//				buffer.put((byte) 255);
//				buffer.put((byte) 255);
//				buffer.put((byte) 255);
//				continue;
//			}
			byte R = (byte) (noise[i] * 255);
			byte G = (byte) (noise[i] * 128);
			byte B = (byte) (noise[i] * 128);
			byte A = (byte) 255;

			buffer.put(A);
			buffer.put(B);
			buffer.put(G);
			buffer.put(R);
		}

		Image image = new Image(Image.Format.ABGR8, size, size, buffer);
		Texture texture = new Texture2D(image);

		return texture;
	}

	private void generateMap(Random random) {
		//Frequency = features. Higher = more features
		float layerF = 0.075f;
		//Weight = smoothness. Higher frequency = more smoothness
		float weight = 1;
		float maxAmplitude = 0;

		SimplexNoise.setRandomPermutation(random);

		for (int i = 0; i < 3; i++) { // octaves
			maxAmplitude += weight;
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					int index = x + y * size;
					noise[index] += (float) SimplexNoise.noise(x * layerF, y * layerF) * weight;
				}
			}
			layerF *= 2f;
			weight *= 0.5f;
		}

		// normalize and fade-out towards the edge
		float halfSize = size / 2.0f;
		for (int x = 0; x < size; x++) {
			float xDistToEdge = Math.abs(halfSize - x) / halfSize;
			for (int y = 0; y < size; y++) {
				float yDistToEdge = Math.abs(halfSize - y) / halfSize;
				float distToEdge = (float) Math.sqrt(xDistToEdge * xDistToEdge + yDistToEdge * yDistToEdge) * 0.75f;

				int index = x + y * size;
				noise[index] = (noise[index] + maxAmplitude) / (maxAmplitude * 2); //normalize to [0, 1]
				noise[index] = Math.max(0, noise[index] - distToEdge); // fade-out
			}
		}
	}
}
