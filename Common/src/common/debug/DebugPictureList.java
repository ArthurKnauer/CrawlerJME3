package common.debug;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Texture3D;
import java.util.ArrayList;
import java.util.Iterator;
import lombok.NonNull;

/**
 *
 * @author VTPlusAKnauer
 */
public class DebugPictureList implements Iterable<DebugPicture> {

	private final static int SIZE = 64;
	private final static int MARGIN = 8;

	private final AssetManager assetManager;
	private final BitmapFont font;

	private final ArrayList<DebugPicture> pictures;
	private final Node guiNode;
	private float currentFreePosition = MARGIN;

	public DebugPictureList(AssetManager assetManager, Node guiNode, BitmapFont font) {
		this.assetManager = assetManager;
		this.font = font;
		this.guiNode = guiNode;
		pictures = new ArrayList<>();
	}

	public void add(@NonNull String name, @NonNull Texture2D texture) {
		DebugPicture picture = new DebugPicture(guiNode, font, name);
		picture.setTexture(assetManager, texture, false);	
		add(picture);
	}
	
	public void add(@NonNull String name, @NonNull Texture3D texture) {
		DebugPicture picture = new DebugPicture(guiNode, font, name);
		picture.setTexture3D(assetManager, texture);	
		add(picture);
	}
	
	private void add(DebugPicture picture) {
		picture.setPosition(currentFreePosition, MARGIN);
		picture.setTextPosition(currentFreePosition, MARGIN + SIZE + font.getCharSet().getRenderedSize());
		picture.setWidth(SIZE);
		picture.setHeight(SIZE);
		picture.updateGeometricState();
		
		pictures.add(picture);
		currentFreePosition += SIZE + MARGIN;
	}

	@Override
	public Iterator<DebugPicture> iterator() {
		return pictures.iterator();
	}

	public void display(ViewPort viewPort, RenderManager renderManager) {
		Camera cam = viewPort.getCamera();
		renderManager.setCamera(cam, true);
		
		for (DebugPicture picture : pictures) {
			renderManager.renderGeometry(picture);
		}

		renderManager.setCamera(cam, false);
	}

	void updateLogicalState(float tpf) {
		for (DebugPicture picture : pictures) {
			picture.updateLogicalState(tpf);
		}
	}
}
