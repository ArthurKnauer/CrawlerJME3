package crawler.test;

import com.sun.speech.freetts.jsapi.FreeTTSEngineCentral;
import java.beans.PropertyVetoException;
import java.util.Locale;
import java.util.logging.Level;
import javax.speech.EngineCreate;
import javax.speech.EngineException;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import lombok.extern.java.Log;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class TextToSpeech {

	private static Synthesizer synth;
	private static boolean initialised = false;
	// define scale frequencies in hertz
	public static final int G1 = 196;
	public static final int A2 = 220;
	public static final int B2 = 246;
	public static final int C2 = 260;
	public static final int D2 = 292;

	private TextToSpeech() {
	}

	public static void main(String[] args) {
		TextToSpeech.init();
		TextToSpeech.speak("testing");
		try {
			synth.waitEngineState(Synthesizer.QUEUE_EMPTY);
		} catch (InterruptedException | IllegalArgumentException ex) {
			log.log(Level.SEVERE, null, ex);
		}
		TextToSpeech.sing("teeeeetees", 300);
	}

	public static void init() {
		if (initialised == false) {
			initialised = true;
			try {
				//try to init text-to-speech //try to init text-to-speech
				SynthesizerModeDesc smd = new SynthesizerModeDesc(null,
																  "general", /* use "time" or "general" */
																  Locale.US,
																  Boolean.FALSE,
																  null);

				FreeTTSEngineCentral central = new FreeTTSEngineCentral();
				EngineList list = central.createEngineList(smd);

				if (list.size() > 0) {
					EngineCreate creator = (EngineCreate) list.get(0);
					synth = (Synthesizer) creator.createEngine();
				}
				else {
					throw new Exception("No available synthesiser voices found");
				}
				synth.allocate();
				synth.resume();

			} catch (Exception ex) {
				log.log(Level.SEVERE, null, ex);
			}
		}
	}

	public static void destroy() {
		try {
			synth.cancelAll();
			synth.waitEngineState(Synthesizer.QUEUE_EMPTY);
			synth.deallocate();
		} catch (InterruptedException | IllegalArgumentException | EngineException ex) {
			log.log(Level.SEVERE, null, ex);
		}
	}

	public static void speak(String text) {
		synth.cancel();
		synth.speakPlainText(text, null);
	}

	public static void sing(String lyrics, int pitch) {
		try {
			synth.getSynthesizerProperties().setPitch(pitch);
			synth.speakPlainText(lyrics, null);
			synth.waitEngineState(Synthesizer.QUEUE_EMPTY);

		} catch (PropertyVetoException | IllegalArgumentException | InterruptedException e) {
			System.err.println(e);
		}
	}
}
