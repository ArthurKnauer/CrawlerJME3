package cityplanner;

import cityplanner.math.BoundingRect;
import cityplanner.mesher.BuildingMesher;
import cityplanner.mesher.RoadMesher;
import cityplanner.mesher.TreeMesher;
import cityplanner.noise.PopulationMap;
import cityplanner.planners.plots.Plot;
import cityplanner.planners.plots.PlotPlanner;
import cityplanner.planners.roads.GlobalGoals;
import cityplanner.planners.roads.LocalConstraints;
import cityplanner.planners.roads.Road;
import cityplanner.planners.roads.RoadPlanner;
import cityplanner.planners.vegetation.Cluster;
import cityplanner.planners.vegetation.Tree;
import cityplanner.planners.vegetation.VegetationPlanner;
import cityplanner.quadtree.Quadtree;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public class CityScape extends Node {

	private float mapSize = 10000;

	private Node debugInfo;

	private Random random;

	private Quadtree quadtree;
	private List<Road> roads;
	private List<Plot> plots;

	private PopulationMap populationMap;

	private ArrayList<Material> facadeMats;
	private Material roofMat;
	private Material roadMat;
	private Material treesMat;

	private final ArrayList<Material> allMaterials;

	private final String matDefFile = "MatDefs/City/City.j3md";
	private final String diffuseMapShaderName = "DiffuseMap";
	private final String sunLightDirShaderName = "SunLightDir";

	public CityScape(String name) {
		super(name);
		allMaterials = new ArrayList<>();
	}

	public void setMapSize(float mapSize) {
		this.mapSize = mapSize;
	}

	public void setSunDirection(Vector3f sunDirection) {
	//	Vector3f sunLightDir = sunDirection.negate();
		for (Material mat : allMaterials) {
			mat.setVector3(sunLightDirShaderName, sunDirection);
		}
	}

	public void build(int seed, AssetManager assetManager) {
		loadMaterials(assetManager);

		setupRandom(seed);
		setupQuadtree();
		buildDebugInfo(seed);
		buildPopulationMap(assetManager);
		buildWorldPlane(assetManager);
		buildRoads();
		buildBuildings();
		buildVegetation();

		toggleDebugInfo(); // hides
	}

	private void loadMaterials(AssetManager assetManager) {
		facadeMats = new ArrayList<>();
		for (int i = 1; i <= 4; i++) {
			Material facadeMat = new Material(assetManager, matDefFile);
			Texture tex = assetManager.loadTexture(new TextureKey("Textures/facades/facade_0" + i + ".dds", false));
			// tex.setMagFilter(Texture.MagFilter.Nearest);
			//	tex.setMinFilter(Texture.MinFilter.NearestLinearMipMap);
			tex.setWrap(Texture.WrapMode.Repeat);
			facadeMat.setTexture(diffuseMapShaderName, tex);

			facadeMats.add(facadeMat);
			allMaterials.add(facadeMat);
		}

		roofMat = new Material(assetManager, matDefFile);
		Texture tex = assetManager.loadTexture(new TextureKey("Textures/facades/roof_01.dds", false));
		//	tex.setMinFilter(Texture.MinFilter.NearestLinearMipMap);
		tex.setWrap(Texture.WrapMode.Repeat);
		roofMat.setTexture(diffuseMapShaderName, tex);
		allMaterials.add(roofMat);

		roadMat = new Material(assetManager, matDefFile);
		tex = assetManager.loadTexture(new TextureKey("Textures/facades/road.dds", false));
		//tex.setMinFilter(Texture.MinFilter.NearestLinearMipMap);
		tex.setWrap(Texture.WrapMode.Repeat);
		roadMat.setTexture(diffuseMapShaderName, tex);
		allMaterials.add(roadMat);

		treesMat = new Material(assetManager, matDefFile);
		tex = assetManager.loadTexture(new TextureKey("Textures/facades/trees.dds", true));
		tex.setWrap(Texture.WrapMode.Repeat);
		treesMat.setTexture(diffuseMapShaderName, tex);
		treesMat.setTransparent(true);
		treesMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
		treesMat.getAdditionalRenderState().setDepthWrite(false);
		allMaterials.add(treesMat);
	}

	private void setupRandom(int seed) {
		random = new Random();
		random.setSeed(++seed);
		random.nextInt(); // somehow the first number is always the same
	}

	private void setupQuadtree() {
		quadtree = new Quadtree(new BoundingRect(new Vector2f(0, 0),
												 new Vector2f(mapSize / 2, mapSize / 2)));
	}

	private void buildDebugInfo(int seed) {
		debugInfo = new Node("debugInfo");

//		BitmapText textNode = buildBillboardText();
//		textNode.setText("" + seed);
//		textNode.setSize(200);
//		textNode.setColor(ColorRGBA.White);
//		textNode.setLocalTranslation(0, 700, 0);
//		debugInfo.attachChild(textNode);
		detachChildNamed("debugInfo");
		attachChild(debugInfo);
	}

	private void buildPopulationMap(AssetManager assetManager) {
		Material material = new Material(assetManager, matDefFile);
		Quad imageQuad = new Quad(mapSize, mapSize);
		Geometry geometry = new Geometry("populationMap", imageQuad);
		geometry.setMaterial(material);
		Quaternion rotation = new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0);
		geometry.setLocalRotation(rotation);
		geometry.setLocalTranslation(-mapSize / 2, -2, mapSize / 2);

		detachChildNamed(diffuseMapShaderName);
		attachChild(geometry);

		populationMap = new PopulationMap(random);
		populationMap.setSizeInMeters(mapSize);

		Texture texture = populationMap.createTexture();
		texture.setMagFilter(Texture.MagFilter.Nearest);
		texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		material.setTexture(diffuseMapShaderName, texture);
	}

	private void buildWorldPlane(AssetManager assetManager) {
		Texture tex = assetManager.loadTexture(new TextureKey("Textures/facades/worldPlane.dds", false));
		tex.setMinFilter(Texture.MinFilter.NearestLinearMipMap);

		Material mat = new Material(assetManager, matDefFile);
		mat.setTexture(diffuseMapShaderName, tex);
		allMaterials.add(mat);

		Quad imageQuad = new Quad(mapSize, mapSize);
		Geometry worldPlaneGeom = new Geometry("worldPlane", imageQuad);
		worldPlaneGeom.setQueueBucket(RenderQueue.Bucket.Sky3D);
		worldPlaneGeom.setMaterial(mat);
		Quaternion rotation = new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0);
		worldPlaneGeom.setLocalRotation(rotation);
		worldPlaneGeom.setLocalTranslation(-mapSize / 2, -1, mapSize / 2);
		worldPlaneGeom.setCullHint(CullHint.Never);

		detachChildNamed("worldPlane");
		attachChild(worldPlaneGeom);
	}

	private void buildRoads() {
		RoadPlanner roadPlanner = new RoadPlanner();
		roadPlanner.setGlobalGoals(new GlobalGoals(random, populationMap));
		roadPlanner.setLocalConstraints(new LocalConstraints(random));
		roads = roadPlanner.plan(quadtree, random.nextInt(50) + 50);
		Geometry roadGeom = RoadMesher.buildGeometry(roads, "roads", random);
		roadGeom.setQueueBucket(RenderQueue.Bucket.Sky3D);
		roadGeom.setMaterial(roadMat);
		roadGeom.setCullHint(Spatial.CullHint.Never);
		detachChildNamed("roads");
		attachChild(roadGeom);
	}

	private void buildBuildings() {
		PlotPlanner plotPlanner = new PlotPlanner(random, populationMap);
		plots = plotPlanner.plan(roads, quadtree);
		Node buildings = new Node("buildings");
		List<Geometry> buildingGeometries = BuildingMesher.buildGeometries(plots, random, facadeMats, roofMat);
		for (Geometry geom : buildingGeometries) {
			buildings.attachChild(geom);
			geom.setQueueBucket(RenderQueue.Bucket.Sky3D);
		}

		buildings.setCullHint(Spatial.CullHint.Never);
		detachChildNamed("buildings");
		attachChild(buildings);
	}

	private void buildVegetation() {
		Node vegetation = new Node("trees");
		detachChildNamed("trees");
		attachChild(vegetation);

		VegetationPlanner vegetationPlanner = new VegetationPlanner(random);
		List<Cluster<Tree>> treeClusters = vegetationPlanner.plan(quadtree);
		List<Geometry> trees = TreeMesher.buildGeometry(treeClusters, "trees", random);
		for (Geometry geom : trees) {
			vegetation.attachChild(geom);
			geom.setMaterial(treesMat);
			geom.setQueueBucket(RenderQueue.Bucket.Transparent);
		}
	}

	public void togglePopulationMap() {
		toggleSpatial("populationMap");
	}

	public void toggleWorldPlane() {
		toggleSpatial("worldPlane");
	}

	public void toggleRoads() {
		toggleSpatial("roads");
	}

	public void toggleBuildings() {
		toggleSpatial("buildings");
	}

	public void toggleTrees() {
		toggleSpatial("trees");
	}

	public void toggleDebugInfo() {
		toggleSpatial("debugInfo");
	}

	private void toggleSpatial(String name) {
		Spatial spatial = getChild(name);
		if (spatial != null) {
			spatial.setCullHint(spatial.getCullHint() == Spatial.CullHint.Always
								? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
		}
	}
}
