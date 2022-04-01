/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.audio;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;

/**
 *
 * @author VTPlusAKnauer
 */
public class AudioNodeEx extends AudioNode {

	public float normalVolume = 1;
	float currentRatio = 0;
	float timePassed = 0;
	float fadeDuration;
	boolean fadingIn = false;

	public AudioNodeEx(AssetManager assetManager, String name, boolean stream) {
		super(assetManager, name, stream);
	}

	public void fadeIn(float time) {
		if (!fadingIn) {
			fadingIn = true;
			fadeDuration = time;
			timePassed = 0;
			setVolume(0);
			stop();
			play();
		}
	}

	public void setMaxVolume(float maxVolume) {
		normalVolume = maxVolume;
	}

	public void fadeOut(float time) {
		if (fadingIn) {
			fadingIn = false;
			timePassed = (1.0f - currentRatio) * time;
			fadeDuration = time;
		}
	}

	public void update(float tpf) {
		if (timePassed < fadeDuration) {
			timePassed += tpf;
			if (timePassed > fadeDuration) {
				timePassed = fadeDuration;
			}

			currentRatio = timePassed / fadeDuration;
			float ratio = currentRatio * currentRatio;
			if (fadingIn) {
				setVolume(ratio * normalVolume);
			}
			else {
				setVolume((1.0f - ratio) * normalVolume);
				if (timePassed == fadeDuration) {
					stop();
				}
			}
		}
	}
}
