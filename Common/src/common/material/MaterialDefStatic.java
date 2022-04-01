package common.material;

import com.jme3.math.Vector3f;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
@Builder
public class MaterialDefStatic {

	@Getter private final String diffuseFile;
	@Getter private final String normalFile;
	@Getter private final String ambientOcclusionFile;
	@Getter private final Vector3f textureScale;
}
