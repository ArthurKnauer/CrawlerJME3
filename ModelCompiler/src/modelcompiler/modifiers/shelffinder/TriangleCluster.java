/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcompiler.modifiers.shelffinder;

import com.jme3.scene.Mesh;
import java.util.List;
import lombok.Value;

/**
 *
 * @author VTPlusAKnauer
 */
@Value
public class TriangleCluster {	
	private Mesh mesh;
	private List<Integer> indices;	
}
