package crawler.characters.ragdoll;

import com.jme3.animation.Bone;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.LinkedList;

public class BoneLink {

	public Bone bone;
	public Node node;
	public RigidBodyControl rigidBody;
	public Quaternion boneBindRot;
	public Vector3f boneToRigidBodyOffset;
	public Vector3f boneBindPos;
	boolean atRestAndUpToDate = false;
	public Quaternion lastRotation;
	public Vector3f lastPosition;
	BoneLink parent = null;
	LinkedList<BoneLink> children = new LinkedList<>();
	LinkedList<Bone> unlinkedBoneChildren = new LinkedList<>();
	public boolean wiggle;
	public int collisionGroup;

	BoneLink(Bone bone, Node node) {
		this.bone = bone;
		this.node = node;
		this.wiggle = true;
		rigidBody = node.getControl(RigidBodyControl.class);
		boneBindPos = bone.getWorldBindInversePosition().mult(-1);
		boneBindRot = bone.getWorldBindInverseRotation().inverse();
		boneToRigidBodyOffset = bone.getWorldBindInversePosition().mult(-1).subtract(node.getWorldTranslation());
		rigidBody.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_NONE); // later groups are added
	}

	public void buildUnlinkedBoneChildrenList(HashMap<String, BoneLink> boneLinkMap) {
		for (Bone childBone : bone.getChildren()) {
			if (!boneLinkMap.containsKey(childBone.getName())) {
				unlinkedBoneChildren.add(childBone);
			}
		}
	}
	
	public ConeJoint createConeJoint(BoneLink child) {
		Vector3f pivotA = node.worldToLocal(child.boneBindPos, new Vector3f());
		Vector3f pivotB = child.node.worldToLocal(child.boneBindPos, new Vector3f());

		ConeJoint joint = new ConeJoint(node.getControl(RigidBodyControl.class),
										child.node.getControl(RigidBodyControl.class),
										pivotA, pivotB);

		joint.setCollisionBetweenLinkedBodys(false);

		joint.setLimit(1.25f, 1.25f, 0.2f);
		return joint;
	}

	public void setCollisionGroup(int collisionGroup) {
		this.collisionGroup = collisionGroup;
		rigidBody.setCollisionGroup(collisionGroup);
	}
	
	public void dontCollideWith(BoneLink link) {
		rigidBody.removeCollideWithGroup(link.collisionGroup);
		link.rigidBody.removeCollideWithGroup(collisionGroup);
	}

	public void addCollideWithGroup(int collisionGroup) {
		rigidBody.addCollideWithGroup(collisionGroup);
	}

	public void matchBoneToRigidBody(Spatial spatial) {
		bone.setUserControl(true);

		if (rigidBody.isActive() || !atRestAndUpToDate) {
			Vector3f position = new Vector3f();
			Quaternion tmpRot1 = new Quaternion();
			Quaternion tmpRot2 = new Quaternion();

			// retrieving rigidBody position in physic world space
			Vector3f rigidBodyPos = rigidBody.getMotionState().getWorldLocation();
			spatial.getWorldTransform().transformInverseVector(rigidBodyPos, position);

			//retrieving rigidBody rotation in physic world space		
			tmpRot1.set(rigidBody.getMotionState().getWorldRotationQuat());
			Vector3f offsetRotated = tmpRot1.mult(boneToRigidBodyOffset);

			spatial.getWorldRotation().inverse().multLocal(offsetRotated);
			position.addLocal(offsetRotated);

			tmpRot1.multLocal(boneBindRot);
			tmpRot2.set(spatial.getWorldRotation()).inverseLocal();
			tmpRot2.mult(tmpRot1, tmpRot1);
			tmpRot1.normalizeLocal();

			setTransform(position, tmpRot1);

			atRestAndUpToDate = !rigidBody.isActive();

			if (atRestAndUpToDate) {
				lastPosition = position;
				lastRotation = tmpRot1;
			}
		}
		else if (atRestAndUpToDate) { // rigidbody at rest and last pos/rot are valid
			setTransform(lastPosition, lastRotation);
		}
	}

	public void matchRigidBodyToBone(Spatial spatial) {
		bone.setUserControl(false);
		//	link.rigidBody.setKinematic(true);

		Quaternion rotation = spatial.getWorldRotation().clone();
		rotation.multLocal(bone.getModelSpaceRotation());
		rotation.multLocal(bone.getWorldBindInverseRotation());

		Vector3f position = spatial.getWorldRotation().mult(bone.getModelSpacePosition());

		Vector3f offsetRotated = boneToRigidBodyOffset.mult(-1);
		rotation.multLocal(offsetRotated);
		position.addLocal(spatial.getWorldTranslation()).addLocal(offsetRotated);

		rigidBody.setPhysicsRotation(rotation);
		rigidBody.setPhysicsLocation(position);
	}

	private void setTransform(Vector3f pos, Quaternion rot) {
		bone.setUserControl(true);
		//we set te user transforms of the bone
		bone.setUserTransformsWorld(pos, rot);
		for (Bone childBone : unlinkedBoneChildren) {
			Transform transform = childBone.getCombinedTransform(pos, rot);
			childBone.setUserControl(true);
			childBone.setUserTransformsWorld(transform.getTranslation(), transform.getRotation());
		}
	}

}
