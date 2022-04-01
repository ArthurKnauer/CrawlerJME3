package crawler.weapons;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import java.util.*;

/**
 *
 * @author VTPlusAKnauer
 */
public class WeaponType {

	private final String name;
	private final int hashCode;
	private final String modelFile;
	private final String textureFile;
	private final String fireSoundFile;

	private final Vector3f muzzlePos;
	private final ColorRGBA muzzleFlashColor;

	private final Set<AmmoType> allowedAmmo;

	public static Builder builder() {
		return new Builder();
	}

	private WeaponType(Builder builder) {
		this.name = builder.name;
		this.hashCode = name.hashCode();
		this.modelFile = builder.modelFile;
		this.textureFile = builder.textureFile;
		this.fireSoundFile = builder.fireSoundFile;

		this.muzzlePos = builder.muzzlePos;
		this.muzzleFlashColor = builder.muzzleFlashColor;

		this.allowedAmmo = Collections.unmodifiableSet(builder.allowedAmmo);
	}

	public String getName() {
		return name;
	}

	public Optional<String> getModelFile() {
		return Optional.ofNullable(modelFile);
	}

	public Optional<String> getTextureFile() {
		return Optional.ofNullable(textureFile);
	}

	public String getFireSoundFile() {
		return fireSoundFile;
	}

	public Set<AmmoType> getAllowedAmmo() {
		return allowedAmmo;
	}

	public Vector3f getMuzzlePos() {
		return muzzlePos;
	}

	public ColorRGBA getMuzzleFlashColor() {
		return muzzleFlashColor;
	}

	@Override
	public String toString() {
		return "WeaponType{" + name + '}';
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WeaponType other = (WeaponType) obj;
		if (this.hashCode != other.hashCode)
			return false;
		if (!Objects.equals(this.name, other.name))
			return false;
		return true;
	}

	public static class Builder {

		private String name;
		private String modelFile;
		private String textureFile;
		private String fireSoundFile;

		private Vector3f muzzlePos = new Vector3f();
		private ColorRGBA muzzleFlashColor = new ColorRGBA(1, 0.5f, 0, 1);

		private Set<AmmoType> allowedAmmo = new HashSet<>(1);

		public void setName(String name) {
			this.name = name;
		}

		public void setModelFile(String modelFile) {
			this.modelFile = modelFile;
		}

		public void setTextureFile(String textureFile) {
			this.textureFile = textureFile;
		}

		public void setFireSoundFile(String fireSoundFile) {
			this.fireSoundFile = fireSoundFile;
		}

		public void setMuzzlePos(float x, float y, float z) {
			muzzlePos.set(x, y, z);
		}

		public void setMuzzleFlashColor(float r, float g, float b, float a) {
			muzzleFlashColor.set(r, g, b, a);
		}

		public void addAllowedAmmo(AmmoType ammoType) {
			this.allowedAmmo.add(ammoType);
		}

		public WeaponType build() {
			if (name == null || name.isEmpty())
				throw new IllegalStateException("name is not set");
			if (modelFile != null && !modelFile.isEmpty() && (textureFile == null || textureFile.isEmpty()))
				throw new IllegalStateException("modelFile is set, but textureFile is not set");
			if (fireSoundFile == null || fireSoundFile.isEmpty())
				throw new IllegalStateException("fireSoundFile is not set");
//			if (allowedAmmo.isEmpty())
//				throw new IllegalStateException("no allowed AmmoType added");

			return new WeaponType(this);
		}
	}

}
