 package crawler.attributes;

import com.jme3.scene.Spatial;
import com.jme3.scene.attribute.AbstractAttribute;
import crawler.main.Globals;
import java.util.HashSet;

/**
 *
 * @author VTPlusAKnauer
 */


public class Inventory extends AbstractAttribute {
	
	HashSet<Spatial> set = new HashSet();

	void take(Spatial spatial) {
		spatial.removeFromParent();
		Globals.getPhysicsSpace().remove(spatial);
		
		set.add(spatial);
	}
	
}
