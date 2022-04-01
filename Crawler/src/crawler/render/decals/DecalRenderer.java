package crawler.render.decals;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture;
import common.render.lpv.DeferredShader;
import common.render.lpv.LPVRenderKit;
import crawler.main.Globals;
import java.util.LinkedList;

/**
 *
 * @author VTPlusAKnauer
 */
public class DecalRenderer {

	private final FrameBuffer frameBuffer;
	private final Material shader;
	private final Geometry decalBox;
	private final Texture diffuseMap;
	//private final Texture decalNormal;

	public DecalRenderer(int screenWidth, int screenHeight, DeferredShader sceneRect) {
		frameBuffer = new FrameBuffer(screenWidth, screenHeight, 1);
		frameBuffer.setMultiTarget(true);
		frameBuffer.addColorTexture(sceneRect.getDiffuseMap());
		frameBuffer.addColorTexture(sceneRect.getNormalMap());

		shader = new Material(Globals.getAssetManager(), "MatDefs/DecalDeferred.j3md");
		shader.setTexture("NormalMap", sceneRect.getNormalMap());
		shader.setTexture("DepthMap", sceneRect.getDepthMap());
		shader.setVector2("ScreenSize", new Vector2f(screenWidth, screenHeight));
		
		diffuseMap = Globals.getAssetManager().loadTexture(new TextureKey("Textures/decals/plaster_hole.dds", false));
		diffuseMap.setWrap(Texture.WrapMode.BorderClamp);
		shader.setTexture("DecalDiffuse", diffuseMap);
		
//		decalNormal = Globals.getAssetManager().loadTexture(new TextureKey("Textures/decals/plaster_hole_normal.dds", false));		
//		decalNormal.setWrap(Texture.WrapMode.BorderClamp);		
//		decalShader.setTexture("DecalNormal", decalNormal);
		
		decalBox = new Geometry("decalBox", new Box(1.0f, 1.0f, 1.0f));
	}

	public void renderDecals(LPVRenderKit kit, LinkedList<Decal> decalList) {
		Camera viewCam = kit.getViewCam();

		shader.setVector3("CameraPos", viewCam.getLocation());
		shader.setVector2("ProjectionValues", new Vector2f(viewCam.getProjectionMatrix().m22, viewCam.getProjectionMatrix().m23));
		shader.setVector3("FrustumLowerLeftRay", kit.getFrustumRays().getLowerLeftRay());
		shader.setVector3("FrustumLowerRightRay", kit.getFrustumRays().getLowerRightRay());
		shader.setVector3("FrustumUpperRightRay", kit.getFrustumRays().getUpperRightRay());
		shader.setVector3("FrustumUpperLeftRay", kit.getFrustumRays().getUpperLeftRay());

		// render decals
		kit.setFrameBuffer(frameBuffer);
		
		shader.setMatrix4("ViewProjectionMatrix", viewCam.getViewProjectionMatrix());

		for (Decal decal : decalList) {
			shader.setMatrix4("DecalToWorld", decal.decalToWorld);
			shader.setMatrix4("WorldToDecal", decal.worldToDecal);
			shader.setFloat("DecalSize", decal.size);
			shader.setFloat("RecipDecalSize", decal.recipSize);
			shader.setVector3("Normal", decal.normal);

			kit.render(shader, decalBox);
		}
	}

}
