package crawler.main;

import cityplanner.CityScape;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import common.material.MaterialLoader;
import crawler.characters.CustomCharacterControl;
import crawler.characters.HealthControl;
import crawler.characters.npc.NPCBuilder;
import crawler.characters.player.Player;
import crawler.interiordesign.InteriorDesigner;
import crawler.items.ItemBuilder;
import crawler.map.MapCreator;
import crawler.navmesh.SuperNavMesh;
import static crawler.properties.CrawlerProperties.PROPERTIES;
import lombok.extern.java.Log;
import skybox.SkyRenderer;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class GameState extends AbstractAppState {

	private MapCreator mapCreator;
	private InteriorDesigner interiorDesigner;
	private Node mapNode, furnitureNode;
	private CityScape cityScape;

	private Player player;
	private Node npc;
	private int seed = 120;
	private final SkyRenderer skyRenderer;

	private static final boolean BUILD_APPCOMPLEX = PROPERTIES.getBoolean("GameState.buildAppComplex");
	private static final boolean BUILD_SKY = PROPERTIES.getBoolean("GameState.buildSky");

	private static final boolean BUILD_CITY = PROPERTIES.getBoolean("GameState.buildCity");
	private static final boolean BUILD_FURNITURE = PROPERTIES.getBoolean("GameState.buildFurniture");
	private static final boolean BUILD_CHARACTERS = PROPERTIES.getBoolean("GameState.buildCharacters");

	public GameState(SkyRenderer skyRenderer) {
		this.skyRenderer = skyRenderer;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		setupCityscape();
		buildMap();
		setupFurniture();

		setupPlayer(app.getCamera());
		placePlayer();
		createCharacters();

		setSunDirection(skyRenderer.getSunDirection());
	}

	public void restart() {
		Globals.getPhysicsSpace().removeAll(player);

		setupCityscape();

		Globals.getRootNode().detachChild(mapNode);
		buildMap();

		placePlayer();

		Globals.getRootNode().detachChild(npc);
		createCharacters();

		setSunDirection(skyRenderer.getSunDirection());
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);
	}

	public void toggleShowNavMesh() {
		mapCreator.toggleShowNavMesh();
	}

	public void toggleZombie() {
	}

	private void buildMap() {
		if (BUILD_APPCOMPLEX) {
			// build apartment (floorplan and then meshes)
			mapCreator = new MapCreator();
			mapNode = mapCreator.create(seed++);
			Globals.getRootNode().attachChild(mapNode);

			// add roof to remove ceiling shadow peter-paning
			BoundingBox mapBBox = (BoundingBox) mapNode.getWorldBound();
			Geometry roof = new Geometry("Roof", new Quad(mapBBox.getXExtent(), mapBBox.getZExtent() - 5));
			roof.setMaterial(MaterialLoader.load("Textures/concrete.dds", "Textures/concrete_normal.dds"));
			roof.setLocalTranslation(-mapBBox.getXExtent() * 0.5f, 3.2f, 2.5f - mapBBox.getZExtent() * 0.5f);
			roof.setLocalRotation(new Quaternion(new float[]{(float) Math.PI * 0.5f, 0, 0}));
			Globals.getRootNode().attachChild(roof);
			roof.setShadowMode(RenderQueue.ShadowMode.Cast);

			Globals.setSuperNavMesh(mapCreator.superNavMesh);
		}
		else {
			Geometry floor = new Geometry("Floor", new Box(40, 1, 40));
			floor.setMaterial(MaterialLoader.load("Textures/concrete.dds", "Textures/concrete_normal.dds"));
			floor.setLocalTranslation(0, -1, 0);
			//floor.setLocalRotation(new Quaternion(new float[]{(float) Math.PI * 0.5f, 0, 0}));
			Globals.getRootNode().attachChild(floor);
			floor.setShadowMode(RenderQueue.ShadowMode.Receive);

			RigidBodyControl rbc = new RigidBodyControl(0);//new BoxCollisionShape(new Vector3f(20, 1, 20)));
			//rbc.setCollideWithGroups(0xffffff);
			floor.addControl(rbc);
			Globals.getPhysicsSpace().add(rbc);
			Globals.setSuperNavMesh(new SuperNavMesh());
		}
	}

	private void setupCityscape() {
		Globals.getRootNode().detachChildNamed("cityScape");
		cityScape = new CityScape("cityScape");

		if (BUILD_CITY) {
			cityScape.setLocalTranslation(0, -150, 0);
			cityScape.build(seed, Globals.getAssetManager());
			cityScape.setCullHint(Spatial.CullHint.Never);
		}
		Globals.getRootNode().attachChild(cityScape);
	}

	private void setupFurniture() {
		if (BUILD_FURNITURE) {
			interiorDesigner = new InteriorDesigner();
			furnitureNode = new Node();
			Globals.getRootNode().attachChild(furnitureNode);
			interiorDesigner.furnishAll(mapCreator.allRooms, furnitureNode, mapCreator.wallSegmentMap);
		}
	}

	private void setupPlayer(Camera camera) {
		player = Player.build(camera);
		Globals.getRootNode().attachChild(player);
		Globals.setPlayer(player);
	}

	private void placePlayer() {
		player.getControl(CustomCharacterControl.class).warp(new Vector3f(16, 0.1f, -6));

		Spatial item = ItemBuilder.buildAndPlace(player.getLocalTranslation().add(0, 1, 0));
		Globals.getRootNode().attachChild(item);
	}

	private void createCharacters() {
		if (BUILD_CHARACTERS) {
			npc = NPCBuilder.start()
					.setModel("Models/characters/kila.mesh.j3o")
					.setMaterial("Textures/characters/kila.dds", "Textures/characters/kila_normal.dds")
					.setRagdoll("Models/characters/kila_ragdoll.mesh.j3o")
					.build();
			Globals.getRootNode().attachChild(npc);
			npc.getControl(CustomCharacterControl.class).warp(new Vector3f(-17f, 0.1f, 7f));
		}
	}

	public void refurnishAllRooms() {
		if (furnitureNode != null && interiorDesigner != null) {
			furnitureNode.detachAllChildren();
			interiorDesigner.furnishAll(mapCreator.allRooms, furnitureNode, mapCreator.wallSegmentMap);
		}

		setupCityscape();

		if (npc != null) {
			npc.getControl(CustomCharacterControl.class).warp(new Vector3f(3f, 0.01f, 2.5f));
			npc.getControl(HealthControl.class).revive();
		}
	}

	private void setSunDirection(Vector3f sunDirection) {
		if (cityScape != null) {
			cityScape.setSunDirection(sunDirection);
		}
	}
}
