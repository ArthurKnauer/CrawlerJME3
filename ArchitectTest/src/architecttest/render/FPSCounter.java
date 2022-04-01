package architecttest.render;

import static java.lang.System.nanoTime;

public class FPSCounter {

	private static final int NUM_DELTAS = 32;
	private static final int[] deltaSmoothingArray = new int[NUM_DELTAS];
	private static int currentIndex;
	private static int currentDeltaSum;
	private static int deltaMilli;
	private static float frameFactor;
	private static long lastFrameTime;
	private static long deltaNano;

	private FPSCounter() {
	}

	public static float getFPS() {
		float averageDelta = (float) currentDeltaSum / (float) NUM_DELTAS;
		return 1000.0f / averageDelta;
	}

	public static int getDelta() {
		return deltaMilli;
	}

	public static float getFrameFactor() {
		return frameFactor;
	}

	public static void newFrame() {
		long thisFrameTime = nanoTime();
		deltaNano = thisFrameTime - lastFrameTime;
		deltaMilli = (int) (deltaNano / 1000000);
		lastFrameTime = thisFrameTime;
		frameFactor = deltaMilli / 20.0f;

		currentDeltaSum -= deltaSmoothingArray[currentIndex];
		deltaSmoothingArray[currentIndex] = deltaMilli;
		currentDeltaSum += deltaMilli;

		currentIndex = (currentIndex + 1) % NUM_DELTAS;
	}
}
