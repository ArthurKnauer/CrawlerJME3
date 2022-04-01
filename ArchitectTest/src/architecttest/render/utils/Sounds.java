/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.utils;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;
import lombok.experimental.UtilityClass;

/**
 *
 * @author AK47
 */
@UtilityClass
public class Sounds {

	public static void play(final String filename) {
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filename));
			AudioFormat format = stream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(stream);
			clip.start();

			try {
				Thread.sleep((long) (clip.getMicrosecondLength() * 0.001));
			} catch (InterruptedException ex) {
			}

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
			throw new RuntimeException(ex);
		}
	}
}
