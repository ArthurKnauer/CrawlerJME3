package crawler.physics.impacts;

import com.jme3.math.Vector3f;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
@Builder
public class Impact {

	@Getter private final Vector3f location;
	@Getter private final Vector3f impulse;
}
