/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

/**
 *
 * @author VTPlusAKnauer
 */
final class LPVTaskList {

	private final List<LPVRenderTask> list = new ArrayList<>(6);
	private int currentTask = 0;

	@Builder
	private LPVTaskList(@NonNull RSMRenderer rsmRenderer,
						@NonNull LPVClearer lpvClearer,
						@NonNull GeometryInjector geometryInjector,
						@NonNull RSMInjector rsmInjector,
						@NonNull LightPointInjector lightPointInjector,
						@NonNull LightPropagator lightPropagator) {
		list.add(rsmRenderer);
		list.add(lpvClearer);
		list.add(geometryInjector);
		list.add(rsmInjector);
		list.add(lightPointInjector);
		list.add(lightPropagator);
	}

	void reset() {
		currentTask = 0;
	}

	void doNext(LPVRenderKit kit) {
		if (currentTask < list.size()) {
			if (list.get(currentTask).doIt(kit) == LPVRenderTask.Result.DONE)
				currentTask++;
		}
	}

}
