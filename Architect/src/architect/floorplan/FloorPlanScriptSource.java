package architect.floorplan;

/**
 *
 * @author VTPlusAKnauer
 */
public class FloorPlanScriptSource implements FloorPlanSource {

	private final String scriptFile;

	public FloorPlanScriptSource(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	@Override
	public FloorPlan getFloorPlan() {
		return FloorPlanBuilder.fromScript(scriptFile);
	}
}
