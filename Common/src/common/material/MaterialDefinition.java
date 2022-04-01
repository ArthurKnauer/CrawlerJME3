package common.material;

import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
@Builder
public class MaterialDefinition {

	@Getter private final String diffuseFile;
	@Getter private final String normalFile;
}
