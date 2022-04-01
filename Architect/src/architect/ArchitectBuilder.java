/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect;

import architect.processors.*;
import architect.processors.connector.RoomConnector;
import architect.processors.openings.DoorCreator;
import architect.processors.openings.WindowCreator;
import architect.processors.protrusion.ProtrusionRemover;
import architect.processors.rrassigner.RoomRectAssigner;
import architect.processors.subdivider.FloorPlanSubdivider;
import architect.processors.wallpressure.WallPressureBuilder;
import architect.processors.wallpressure.WallPressureRelaxer;
import java.util.ArrayList;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author VTPlusAKnauer
 */
public class ArchitectBuilder {

	private final ArrayList<FloorPlanProcessor> processorList;

	private ArchitectBuilder() {
		processorList = new ArrayList<>();
	}

	public static Architect fromScript(String fileName) {
		LuaValue globals = JsePlatform.standardGlobals();
		globals.get("dofile").call(LuaValue.valueOf(fileName));
		LuaFunction createArchitect = (LuaFunction) globals.get("createArchitect");
		return (Architect) createArchitect.call().touserdata(Architect.class);
	}

	public static ArchitectBuilder start() {
		return new ArchitectBuilder();
	}

	public Architect build() {
		if (processorList.isEmpty())
			throw new IllegalStateException("Trying to create an architect with an empty processor list");
		return new Architect(processorList);
	}

	public ArchitectBuilder addFloorPlanSubdivider() {
		processorList.add(new FloorPlanSubdivider());
		return this;
	}

	public ArchitectBuilder addProtoRoomAssigner() {
		processorList.add(new ProtoRoomAssigner());
		return this;
	}

	public ArchitectBuilder addRoomRectAssigner() {
		processorList.add(new RoomRectAssigner());
		return this;
	}

	public ArchitectBuilder addProtrusionRemover() {
		processorList.add(new ProtrusionRemover());
		return this;
	}

	public ArchitectBuilder addWallPressureBuilder(boolean canPressureHallway) {
		processorList.add(new WallPressureBuilder(canPressureHallway));
		return this;
	}

	public ArchitectBuilder addWallPressureRelaxer() {
		processorList.add(new WallPressureRelaxer());
		return this;
	}

	public ArchitectBuilder addRoomTypeAssigner() {
		processorList.add(new RoomTypeAssigner());
		return this;
	}

	public ArchitectBuilder addRoomConnector() {
		processorList.add(new RoomConnector());
		return this;
	}

	public ArchitectBuilder addRoomMerger() {
		processorList.add(new RoomMerger());
		return this;
	}

	public ArchitectBuilder addDoorCreator() {
		processorList.add(new DoorCreator());
		return this;
	}

	public ArchitectBuilder addWindowCreator() {
		processorList.add(new WindowCreator());
		return this;
	}

	public ArchitectBuilder addStatisticsAnalyzer() {
		processorList.add(new StatisticsAnalyzer());
		return this;
	}
}
