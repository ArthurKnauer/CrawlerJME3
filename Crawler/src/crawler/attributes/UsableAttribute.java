package crawler.attributes;

import com.jme3.scene.Spatial;
import com.jme3.scene.attribute.AbstractAttribute;

/**
 *
 * @author VTPlusAKnauer
 */


public abstract class UsableAttribute extends AbstractAttribute {
	
	public abstract void use(Spatial user);
	
}
