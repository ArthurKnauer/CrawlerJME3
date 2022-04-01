package crawler.attributes;

import com.jme3.scene.Spatial;

/**
 *
 * @author VTPlusAKnauer
 */
public class PickupAttribute extends UsableAttribute {

	@Override
	public void use(Spatial user) {
		Inventory inventory = user.getAttribute(Inventory.class);
		if (inventory == null)
			throw new RuntimeException("User " + user + " has no inventory, cannot pickup " + spatial);

		inventory.take(spatial);
	}

}
