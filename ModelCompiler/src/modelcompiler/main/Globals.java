package modelcompiler.main;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.scene.Node;
import common.material.RandomMaterialSelector;
import common.render.lpv.LPVProcessor;
import java.util.Random;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author VTPlusAKnauer
 */
public class Globals {

	@Getter @Setter(AccessLevel.PACKAGE) private static Random random;
	@Getter @Setter(AccessLevel.PACKAGE) private static Node rootNode;
	@Getter @Setter(AccessLevel.PACKAGE) private static AssetManager assetManager;
	@Getter @Setter(AccessLevel.PACKAGE) private static LPVProcessor lpvProcessor;
	@Getter @Setter(AccessLevel.PACKAGE) private static BitmapFont defaultFont;
	@Getter @Setter(AccessLevel.PACKAGE) private static RandomMaterialSelector materialSelector;

	private Globals() {
	}

}
