package crawler.main;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.font.BitmapFont;
import com.jme3.scene.Node;
import common.material.RandomMaterialSelector;
import common.render.lpv.LPVProcessor;
import crawler.characters.player.Player;
import crawler.navmesh.SuperNavMesh;
import crawler.weapons.WeaponTypeSet;
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
	@Getter @Setter(AccessLevel.PACKAGE) private static BulletAppState bulletAppState;
	@Getter @Setter(AccessLevel.PACKAGE) private static PhysicsSpace physicsSpace;
	@Getter @Setter(AccessLevel.PACKAGE) private static LPVProcessor lpvProcessor;
	@Getter @Setter(AccessLevel.PACKAGE) private static BitmapFont defaultFont;
	@Getter @Setter(AccessLevel.PACKAGE) private static WeaponTypeSet weaponTypeSet;
	@Getter @Setter(AccessLevel.PACKAGE) private static SuperNavMesh superNavMesh;
	@Getter @Setter(AccessLevel.PACKAGE) private static Player player;
	@Getter @Setter(AccessLevel.PACKAGE) private static RandomMaterialSelector materialSelector;

	private Globals() {
	}

}
