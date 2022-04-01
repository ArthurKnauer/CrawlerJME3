/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcompiler.main;

import com.jme3.app.state.AbstractAppState;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author VTPlusAKnauer
 */
public class TriangleClusterToggler extends AbstractAppState {

	private static final float TIME_BETWEEN_TOGGLES = 0.2f;

	private float timeSinceLastToggle = 0;
	private int currentShownCluster;

	private final Node clusterNode;

	public TriangleClusterToggler(Node clusterNode) {
		this.clusterNode = clusterNode;
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);
		toggleShowCluster(tpf);
	}

	private void toggleShowCluster(float tpf) {
		if (!clusterNode.getChildren().isEmpty()) {
			
			timeSinceLastToggle += tpf;
			if (timeSinceLastToggle > TIME_BETWEEN_TOGGLES) {
				timeSinceLastToggle = 0;

				currentShownCluster = (currentShownCluster + 1) % clusterNode.getChildren().size();
				for (int i = 0; i < clusterNode.getChildren().size(); i++) {
					Spatial child = clusterNode.getChild(i);
					if (currentShownCluster != i)
						child.setCullHint(Spatial.CullHint.Always);
					else
						child.setCullHint(Spatial.CullHint.Inherit);
				}
			}
		}
	}

}
