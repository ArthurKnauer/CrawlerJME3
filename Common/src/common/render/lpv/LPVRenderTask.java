/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

/**
 *
 * @author VTPlusAKnauer
 */
interface LPVRenderTask {
	
	enum Result {DONE, NEEDS_MORE_CALLS};

	void setLPVShape(LPVShape lpvShape);

	Result doIt(LPVRenderKit kit);
}
