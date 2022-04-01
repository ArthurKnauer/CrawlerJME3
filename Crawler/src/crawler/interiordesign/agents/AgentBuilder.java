package crawler.interiordesign.agents;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import common.material.MaterialLoader;
import crawler.interiordesign.RandomIterableList;
import crawler.main.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 *
 * @author VTPlusAKnauer
 */
public class AgentBuilder extends OrientedAgentBoxBuilder {

	Spatial spatial;

	int type = -1;

	RandomIterableList<Integer> parentTypes;
	RandomIterableList<Side> attachSides;
	Placement placement;

	public static AgentBuilder start() {
		return new AgentBuilder();
	}

	public void setModel(String modelFile) {
		spatial = Globals.getAssetManager().loadModel(modelFile);
		Geometry geometry = (Geometry) ((Node) spatial).getChild(0);

		geometry.setModelBound(new BoundingBox());
		geometry.updateModelBound();

		BoundingBox bbox = (BoundingBox) geometry.getModelBound();
		extents = new Vector3f();
		bbox.getExtent(extents);
	}

	public void setMaterial(String textureFile, boolean transparent) {
		if (spatial == null)
			throw new IllegalStateException("Trying to set material before model");

		Material mat;

		if (transparent) { // use MaterialLoader to load transparent mat
			spatial.setQueueBucket(RenderQueue.Bucket.Transparent);
			spatial.setShadowMode(RenderQueue.ShadowMode.Off);

			mat = new Material(Globals.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			Texture tex = Globals.getAssetManager().loadTexture(new TextureKey(textureFile, false));
			tex.setWrap(Texture.WrapMode.Repeat);
			mat.setTexture("ColorMap", tex);
		}
		else {
			spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
			mat = MaterialLoader.load(textureFile);
		}

		spatial.setMaterial(mat);
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setParentTypes(LuaTable parentTypes) {
		this.parentTypes = new RandomIterableList<>(parentTypes.length());
		for (LuaValue key : parentTypes.keys()) {
			this.parentTypes.add(parentTypes.get(key).toint());
		}
	}

	public void setAttachSides(LuaTable attachSides) {
		this.attachSides = new RandomIterableList<>(attachSides.length());
		for (LuaValue key : attachSides.keys()) {
			this.attachSides.add((Side) attachSides.get(key).touserdata(Side.class));
		}
	}

	public void setPlacement(Placement placement) {
		this.placement = placement;
	}

	public Agent build() {
		if (extents == null)
			throw new IllegalStateException("Trying to create Agent without extents");
		if (type < 0)
			throw new IllegalStateException("Trying to create Agent with negative type");

		return new Agent(this);
	}
}
