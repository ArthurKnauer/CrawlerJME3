package crawler.characters.ragdoll;

import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class RagdollBuilder {
	
	public static void createBoneLinksFromRagdollMesh(Mesh ragdollMesh, Skeleton skeleton, Object userObject,
													  RagdollControl rdc, LinkedList<BoneLink> boneLinks,
													  LinkedList<BoneLink> rootBoneLinks,
													  HashMap<String, BoneLink> boneLinkMap) {
		FloatBuffer vertices = ragdollMesh.getFloatBuffer(VertexBuffer.Type.Position);
		ByteBuffer boneIndices = (ByteBuffer) ragdollMesh.getBuffer(VertexBuffer.Type.BoneIndex).getData();
		FloatBuffer boneWeight = (FloatBuffer) ragdollMesh.getBuffer(VertexBuffer.Type.BoneWeight).getData();
		vertices.rewind();
		boneIndices.rewind();
		boneWeight.rewind();

		HashMap<Integer, ArrayList<Float>> bonePointMap = new HashMap<>();

		// put each vertex into an element list for a bone (submesh in ragdoll mesh) -> compute convex hull later
		int vertexCount = ragdollMesh.getVertexCount();
		for (int v = 0; v < vertexCount; v++) {
			int boneIndex = boneIndices.get(v * 4);  // each vertex has 4 boneweights, take first
			ArrayList<Float> points = bonePointMap.get(boneIndex);
			if (points == null) { // bone has no point list yet
				points = new ArrayList<>(8 * 3); // cube is most of the time the smallest shape -> 8 corners
				bonePointMap.put(boneIndex, points);
			}
			points.add(vertices.get(v * 3 + 0));
			points.add(vertices.get(v * 3 + 1));
			points.add(vertices.get(v * 3 + 2));
		}

		for (Map.Entry<Integer, ArrayList<Float>> entry : bonePointMap.entrySet()) {
			Bone bone = skeleton.getBone(entry.getKey());

			Vector3f bonePos = bone.getWorldBindInversePosition().mult(-1);
			// create convex collision hull from submesh points
			float points[] = new float[entry.getValue().size()];
			double averageX = 0;
			double averageY = 0;
			double averageZ = 0;
			for (int p = 0; p < entry.getValue().size(); p += 3) {
				points[p + 0] = entry.getValue().get(p + 0) - bonePos.x;
				points[p + 1] = entry.getValue().get(p + 1) - bonePos.y;
				points[p + 2] = entry.getValue().get(p + 2) - bonePos.z;

				averageX += points[p + 0];
				averageY += points[p + 1];
				averageZ += points[p + 2];
			}

			// compute center of rigid body -> for center of mass
			Vector3f centerOffset = new Vector3f((float) (averageX / (entry.getValue().size() / 3)),
												 (float) (averageY / (entry.getValue().size() / 3)),
												 (float) (averageZ / (entry.getValue().size() / 3)));
			// shift hull
			for (int p = 0; p < points.length; p += 3) {
				points[p + 0] -= centerOffset.x;
				points[p + 1] -= centerOffset.y;
				points[p + 2] -= centerOffset.z;
			}

			bonePos.addLocal(centerOffset);
			HullCollisionShape shape = new HullCollisionShape(points);
			Node node = new Node(bone.getName() + " RB");
			RigidBodyControl rbc = new RigidBodyControl(shape, 3.0f); // default mass 3kg -> will be changed later
			rbc.setDamping(0.9f, 0.999f);
			rbc.setSleepingThresholds(5.0f, 5.0f);

			node.setLocalTranslation(bonePos);
			node.addControl(rbc);
			rbc.setUserObject(userObject); // do this after node.addControl, because node sets its own userObject

			BoneLink link = new BoneLink(bone, node);
			boneLinks.add(link);
			boneLinkMap.put(bone.getName(), link);
		}

		// put each collisionshape in own collision group (parent and child will ignore collision -> joint constraint will take care)
		int collisionGroup = PhysicsCollisionObject.COLLISION_GROUP_02;
		int allCollisionGroups = 0;

		for (BoneLink link : boneLinks) {
			if (collisionGroup == PhysicsCollisionObject.COLLISION_GROUP_01) { // skip 1: biped capsule
				collisionGroup = collisionGroup << 1;
				log.log(Level.WARNING, "Too many rigid bodies, have multiple bodies with same collision group");
			}
			allCollisionGroups |= collisionGroup;
			link.setCollisionGroup(collisionGroup);
			//link.rigidBody.setCollideWithGroups(collisionGroup);
			collisionGroup = collisionGroup << 1;
		}

		// connect children with parents
		for (BoneLink link : boneLinks) {
			Bone parent = link.bone.getParent();
			link.addCollideWithGroup(allCollisionGroups);
			while (parent != null) {
				if (boneLinkMap.containsKey(parent.getName()))
					break;
				else
					parent = parent.getParent();
			}

			if (parent != null) {
				BoneLink parentLink = boneLinkMap.get(parent.getName());
				parentLink.children.add(link);
				link.parent = parentLink;
			}
			else {
				rootBoneLinks.add(link);
			}
		}
	}
}
