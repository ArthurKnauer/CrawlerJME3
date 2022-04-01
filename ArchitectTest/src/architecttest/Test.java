package architecttest;

import architect.BuildingArchitect;
import architect.floorplan.FloorPlan;
import architect.logger.LogManager;
import architect.processors.FloorPlanProcessor;
import architect.processors.connector.RoomConnector;
import architect.processors.helpers.Deisolator;
import architect.processors.helpers.Simplifier;
import architect.processors.protrusion.ProtrusionRemover;
import architect.processors.rrassigner.RoomRectAssigner;
import architect.processors.wallpressure.WallPressureRelaxer;
import architect.utils.UniqueID;
import architecttest.input.KeyboardInput;
import architecttest.input.MouseInput;
import architecttest.render.CheckRects;
import architecttest.render.FPSCounter;
import architecttest.render.drawers.complex.ComplexRenderer;
import architecttest.render.drawers.complex.DrawerInfo;
import architecttest.render.drawers.floorplan.FloorPlanRenderer;
import architecttest.render.text.GLText;
import architecttest.render.utils.GLManager;
import architecttest.render.utils.OrbitCamera;
import architecttest.render.utils.Sounds;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import lombok.Getter;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

public class Test {

	private final static Logger LOGGER = Logger.getLogger(Test.class.getName());
	
	private final Random rand = new Random();
	private BuildingArchitect buildingArchitect = new BuildingArchitect(rand, "../ArchitectScripts");
	
	@Getter private static int seed = 123;

	private final KeyboardInput keyboardInput = new KeyboardInput();
	private final MouseInput mouseInput = new MouseInput();

	private final OrbitCamera camera;
	private final FloorPlanRenderer floorPlanRenderer = new FloorPlanRenderer(keyboardInput);
	private final ComplexRenderer mainRenderer = new ComplexRenderer(keyboardInput);
	private final CheckRects checkRects = new CheckRects();

	public static void main(String args[]) throws Exception {
		try {
			 System.out.println("Working Directory = " + System.getProperty("user.dir"));
			 
			LogManager.setup();
			LOGGER.setLevel(Level.ALL);
		} catch (IOException ex) {
			throw new RuntimeException("Problems with creating the log files", ex);
		}

		Test instance = new Test();
		instance.run();
	}

	private Test() {
		keyboardInput.bindMethod("CloseProgram", this::exit);

		keyboardInput.bindMethod("ResolveRoomRectIsolation", this::resolveRoomRectIsolation);
		keyboardInput.bindMethod("SimplifyAll", this::simplifyAll);
		keyboardInput.bindMethod("EnterNewSeed", this::enterNewSeed);
		keyboardInput.bindMethod("ResetCamera", this::resetCamera);
		
		keyboardInput.bindMethod("NextProcessor", this::nextProcessor);
		keyboardInput.bindMethod("NextSubFloorPlan", this::nextSubFloorPlan);
		keyboardInput.bindMethod("NextProcessorSubStep", this::nextProcessorSubStep);
		
		//keyboardInput.bindMethod("NextBuilding", this::nextBuilding);
		

		
		keyboardInput.bindMethod("PreviousSeed", this::previousSeed);
		keyboardInput.bindMethod("NextSeed", this::nextSeed);
		
		//keyboardInput.bindMethod("NextSeedAndProcessToSamePosition", this::nextSeedAndProcessToSamePosition);
		keyboardInput.bindMethod("ProcessNextMultipleFloorplans", this::processNextMultipleFloorplans);
		keyboardInput.bindMethod("AddCheckRectangle", checkRects::add);
		keyboardInput.bindMethod("RemoveLastCheckRectangle", checkRects::removeLast);

		camera = new OrbitCamera(new Vector3f(0, 0, 0),
								 new Vector3f(0, 0, 10),
								 new Vector3f(0, 1, 0), 1.0f,
								 100.0f, 78.0f, 1);
	}

	@SuppressWarnings("SleepWhileInLoop")
	public void run() {
		try {
			GLManager.createWindow("ApartmentArchitect");
			GLManager.init();
			GLText.buildFont();

			newFloorPlan(seed);

			while (!Display.isCloseRequested()) {
				FPSCounter.newFrame();

				doLogic();
				render();
				Thread.sleep(50);
			}

			GLManager.destroy();

		} catch (Exception e) {
			System.err.println("seed: " + seed);
			System.err.println(e);
			Sounds.play("errorsounds\\error (" + rand.nextInt(42) + ").wav");
			throw new RuntimeException(e);
		}
	}

	private void render() {
		GLManager.beginRendering(camera);
		GLText.setScale(5 * camera.verticalFOV / 75.0f);

		glDisable(GL_TEXTURE_2D);
		glDisable(GL_LIGHTING);

		floorPlanRenderer.draw(buildingArchitect.getComplexPlan());

		for (FloorPlan floorPlan : buildingArchitect.getSubFloorPlans()) {
			floorPlanRenderer.draw(floorPlan);
		}

		DrawerInfo mainInfo = DrawerInfo.build(buildingArchitect.getSubFloorPlans(), 
											   buildingArchitect.getComplexPlan(), 
											   buildingArchitect.getCurrentArchitect(), 
											   mouseInput.getMouseWorldPosition());
		
		mainRenderer.draw(mainInfo);
		mainRenderer.drawText(mainInfo);

		checkRects.draw();

		GLManager.endRendering();
	}

	private void newFloorPlan(int newSeed) {
		UniqueID.resetAll();

		seed = newSeed;
		rand.setSeed(seed);
	
		 System.out.println("Working Directory = " + System.getProperty("user.dir"));
		buildingArchitect = new BuildingArchitect(rand, "../ArchitectScripts");
	}

	private void doLogic() throws Exception {
		mouseInput.process(camera);
		keyboardInput.process();
	}

	public void resolveRoomRectIsolation() {
		Deisolator.resolveRoomRectIsolation(buildingArchitect.getComplexPlan().rooms);
	}

	private void simplifyAll() {
		Simplifier.simplifyAll(buildingArchitect.getComplexPlan());
	}

	private void enterNewSeed() {
		String inputText = (String) JOptionPane.showInputDialog(null, "Enter new seed:", "New Seed",
																JOptionPane.PLAIN_MESSAGE, null, null, "0");
		try {
			int newSeed = Integer.parseInt(inputText);
			newFloorPlan(newSeed);
		} catch (NumberFormatException e) {
		}
	}

	private void resetCamera() {
		camera.position.x = 0;
		camera.position.y = 0;
		camera.verticalFOV = 90;
	}

	private void nextProcessor() {
		buildingArchitect.nextStep(rand);
	}

	private void nextSubFloorPlan() {
		buildingArchitect.nextSubFloorPlan(rand);
	}
	
	private void nextBuilding() {
		buildingArchitect.buildAll(rand);
	}

	private void nextProcessorSubStep() {
		FloorPlanProcessor proc = buildingArchitect.getCurrentArchitect().getCurrentProcessor();
		if (proc != null) {
			if (proc.getClass() == ProtrusionRemover.class)
				((ProtrusionRemover) proc).removeProtrusionsStep();
			else if (proc.getClass() == RoomRectAssigner.class)
				((RoomRectAssigner) proc).addBestRR();
			else if (proc.getClass() == WallPressureRelaxer.class)
				((WallPressureRelaxer) proc).relaxRoomOutwardPressures();
			else if (proc.getClass() == RoomConnector.class)
				((RoomConnector) proc).connectPublicRoomStep();
		}
	}

	private void previousSeed() {
		newFloorPlan(seed - 1);
	}

	private void nextSeed() {
		newFloorPlan(seed + 1);
	}

	private void nextSeedAndProcessToSamePosition() {
//		int lastStepReached = architect.getCurrentStep();
//		newFloorPlan(++seed);
//		architect.runProcessorsUntilStep(rand, floorPlan, lastStepReached);
	}

	private void processNextMultipleFloorplans() {
//		for (int i = 0; i < 10000; i++) {
//			newFloorPlan(++seed);
//			architect.runAllProcessors(rand, floorPlan);
//			if (Validator.findErrors(floorPlan))
//				break; // errors found
//			System.out.print(".");
//			if (i % 40 == 0)
//				System.out.println(" (" + seed + ")");
//		}
	}

	private void exit() {
		GLManager.destroy();
		System.exit(0);
	}

}
