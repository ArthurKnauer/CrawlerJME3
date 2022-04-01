package crawler.debug;

import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.debug.Arrow;
import com.jme3.util.BufferUtils;
import crawler.interiordesign.agents.OrientedBoundingBox;
import crawler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */
public class GeometryBuilder {

	private GeometryBuilder() {
	}

	public static Spatial buildWireCube(BoundingBox bbox, ColorRGBA color) {
		Vector3f vertices[] = {bbox.getCenter().add(bbox.getXExtent(), bbox.getYExtent(), bbox.getZExtent()),
							   bbox.getCenter().add(bbox.getXExtent(), bbox.getYExtent(), -bbox.getZExtent()),
							   bbox.getCenter().add(bbox.getXExtent(), -bbox.getYExtent(), bbox.getZExtent()),
							   bbox.getCenter().add(bbox.getXExtent(), -bbox.getYExtent(), -bbox.getZExtent()),
							   bbox.getCenter().add(-bbox.getXExtent(), bbox.getYExtent(), bbox.getZExtent()),
							   bbox.getCenter().add(-bbox.getXExtent(), bbox.getYExtent(), -bbox.getZExtent()),
							   bbox.getCenter().add(-bbox.getXExtent(), -bbox.getYExtent(), bbox.getZExtent()),
							   bbox.getCenter().add(-bbox.getXExtent(), -bbox.getYExtent(), -bbox.getZExtent())};

		short indices[] = {0, 1, 0, 2, 1, 3, 2, 3, // X+
						   4, 5, 4, 6, 5, 7, 6, 7, // X-
						   0, 4, 6, 2, 1, 5, 3, 7}; // rest 4 lines

		return buildLines(vertices, indices, color);
	}

	public static Spatial buildWireCube(OrientedBoundingBox bbox, ColorRGBA color) {
		Vector3f vertices[] = bbox.getCorners();

		short indices[] = {0, 1, 0, 2, 1, 3, 2, 3, // X+
						   4, 5, 4, 6, 5, 7, 6, 7, // X-
						   0, 4, 6, 2, 1, 5, 3, 7}; // rest 4 lines

		return buildLines(vertices, indices, color);
	}

	public static Geometry buildArrow(ColorRGBA color) {
		Arrow arrow = new Arrow(Vector3f.UNIT_Z);
		arrow.setLineWidth(2);

		Geometry geometry = new Geometry("arrow", arrow);
		Material material = new Material(Globals.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		material.getAdditionalRenderState().setWireframe(true);
		material.setColor("Color", color);
		geometry.setMaterial(material);
		
		return geometry;
	}

	public static BitmapText buildText(String text, Vector3f location, ColorRGBA color) {
		BitmapText bmText = new BitmapText(Globals.getDefaultFont(), false);
		bmText.setSize(0.1f);
		bmText.setText(text);
		bmText.setColor(color);

		// Add a BillboardControl so it rotates towards the cam
		BillboardControl bbControl = new BillboardControl();
		bbControl.setAlignment(BillboardControl.Alignment.Screen);
		bmText.setLocalTranslation(location);
		bmText.addControl(bbControl);

		return bmText;
	}

	public static Spatial buildLineStrip(Vector3f[] vertices, ColorRGBA color) {
		return buildLines(vertices, indicesForVertices(vertices), color);
	}

	public static Spatial buildLines(Vector3f[] vertices, short[] indices, ColorRGBA color) {
		Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Lines);
		mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		mesh.setBuffer(VertexBuffer.Type.Index, 2, indices);
		mesh.setLineWidth(1);
		mesh.updateBound();
		mesh.updateCounts();

		Geometry lines = new Geometry("GeometryBuilder lines", mesh);
		Material mat = new Material(Globals.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", color);
		lines.setMaterial(mat);
		return lines;
	}

	public static Spatial buildPoints(Vector3f[] vertices, ColorRGBA color) {
		Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Points);
		mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		mesh.setBuffer(VertexBuffer.Type.Index, 2, indicesForVertices(vertices));
		mesh.updateBound();
		mesh.updateCounts();
		mesh.setPointSize(3);

		Geometry points = new Geometry("points", mesh);
		Material mat = new Material(Globals.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", color);
		points.setMaterial(mat);
		return points;
	}

	private static short[] indicesForVertices(Vector3f[] vertices) {
		short[] indices = new short[2 * vertices.length]; //Indexes are in pairs, from a vertex and to a vertex

		for (short i = 0; i < vertices.length - 1; i++) {
			indices[2 * i] = i;
			indices[2 * i + 1] = (short) (i + 1);
		}

		return indices;
	}
}
