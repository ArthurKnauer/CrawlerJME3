/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.characters.ragdoll;

import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author VTPlusAKnauer
 */
public class RagdollControl implements PhysicsControl {

	private Spatial spatial;
	private final Spatial ragdoll;
	private Skeleton skeleton;
	private SkeletonControl skeletonControl;
	private PhysicsSpace space;
	private final Object userObject; // user object which will be set in each rigid body -> can be used to get the parent (e.g. monster)
	private boolean enabled = true;

	private final LinkedList<BoneLink> boneLinks = new LinkedList<>();
	private final LinkedList<BoneLink> rootBoneLinks = new LinkedList<>();
	private final HashMap<String, BoneLink> boneLinkMap = new HashMap<>();
	private final LinkedList<PhysicsJoint> joints = new LinkedList<>();

	public RagdollControl(Spatial ragdoll, Object userObject) {
		this.ragdoll = ragdoll;
		this.userObject = userObject;
	}

	public RigidBodyControl getFirstRigidBodyControl() {
		return boneLinkMap.entrySet().iterator().next().getValue().rigidBody;
	}

	@Override
	public void setPhysicsSpace(PhysicsSpace space) {
		this.space = space;

		for (BoneLink link : boneLinks) {
			space.add(link.node);
		}
		for (PhysicsJoint joint : joints) {
			space.add(joint);
		}
	}

	@Override
	public PhysicsSpace getPhysicsSpace() {
		return space;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean state) {
		if (state != enabled) {
			enabled = state;

			if (enabled) {
				for (BoneLink link : boneLinks) {
					link.rigidBody.setMass(2);
					link.matchRigidBodyToBone(spatial);
					link.rigidBody.activate();
				}
			}
		}
	}

	@Override
	public Control cloneForSpatial(Spatial spatial) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
		Mesh ragdollMesh = ((Geometry) ((Node) ragdoll).getChild(0)).getMesh();

		skeletonControl = spatial.getControl(SkeletonControl.class);
		skeleton = skeletonControl.getSkeleton();

		RagdollBuilder.createBoneLinksFromRagdollMesh(ragdollMesh, skeleton, userObject, this, boneLinks, rootBoneLinks, boneLinkMap);
		
		for (BoneLink link : boneLinks) {
			link.buildUnlinkedBoneChildrenList(boneLinkMap);
		}
		
		LuaFunction buildJoints = compileLuaFunction(functionNameFromSpatial(spatial));
		buildJoints.call(CoerceJavaToLua.coerce(this));
	}
	
	private static String functionNameFromSpatial(Spatial spatial) {		
		String name = spatial.getName();
		String suffix = "-ogremesh";
		if (!name.endsWith(suffix))
			throw new IllegalArgumentException("Spatial '" + spatial + "' is expected to end with '" +  suffix + "'");
		
		String functionName = name.substring(0, name.length() - suffix.length());
		System.out.println("spatial to ragdoll func name: " + name + " -> " + functionName);
		return functionName;
	}
	
	private static LuaFunction compileLuaFunction(String name) {
		LuaValue globals = JsePlatform.standardGlobals();
		globals.get("dofile").call(LuaValue.valueOf("assets/lua/ragdoll.lua"));
		return (LuaFunction) globals.get(name);
	}
	
	public BoneLink getBoneLink(String name) {
		return boneLinkMap.get(name);
	}
	
	public void addJoint(PhysicsJoint joint) {
		joints.add(joint);
	}

	public void resetBonePositions() {
		skeleton.resetAndUpdate();

		for (BoneLink link : boneLinks) {
			link.matchRigidBodyToBone(spatial);

			link.rigidBody.clearForces();
			if (link.rigidBody.getMass() > 0) {
				link.rigidBody.setAngularVelocity(Vector3f.ZERO);
				link.rigidBody.setLinearVelocity(Vector3f.ZERO);
			}
		}
	}

	@Override
	public void update(float tpf) {
		if (enabled) {
			for (BoneLink link : boneLinks) {
				if (link.wiggle) {
					link.rigidBody.setMass(2);
					link.matchBoneToRigidBody(spatial);
				}
				else {
					link.rigidBody.setMass(0);
					link.matchRigidBodyToBone(spatial);
				}
			}
		}
		else {
			for (BoneLink link : boneLinks) {
				link.rigidBody.setMass(0);
				link.matchRigidBodyToBone(spatial);
			}
		}
	}

	@Override
	public void render(RenderManager rm, ViewPort vp) {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}	
}
