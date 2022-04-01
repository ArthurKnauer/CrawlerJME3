package crawler.weapons;

import java.util.Objects;

/**
 *
 * @author VTPlusAKnauer
 */


public class AmmoType {
	
	private final String name;
	private final int hashCode;
	private final String modelFile;
	private final String textureFile;
	private final int projectilesPerShot;
	private final float damage;	
	
	public static Builder builder() {
		return new Builder();
	}

	private AmmoType(Builder builder) {
		this.name = builder.name;
		this.hashCode = name.hashCode();
		this.modelFile = builder.modelFile;
		this.textureFile = builder.textureFile;
		this.projectilesPerShot = builder.projectilesPerShot;
		this.damage = builder.damage;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return "AmmoType{" + name + '}';
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AmmoType other = (AmmoType) obj;
		if (!Objects.equals(this.name, other.name))
			return false;
		if (!Objects.equals(this.modelFile, other.modelFile))
			return false;
		if (!Objects.equals(this.textureFile, other.textureFile))
			return false;
		if (this.projectilesPerShot != other.projectilesPerShot)
			return false;
		if (Float.floatToIntBits(this.damage) != Float.floatToIntBits(other.damage))
			return false;
		return true;
	}

	public static class Builder {
		private String name;
		private String modelFile;
		private String textureFile;
		private int projectilesPerShot = 1;
		private float damage = 0;		

		public void setName(String name) {
			this.name = name;
		}

		public void setModelFile(String modelFile) {
			this.modelFile = modelFile;
		}

		public void setTextureFile(String textureFile) {
			this.textureFile = textureFile;
		}

		public void setProjectilesPerShot(int projectilesPerShot) {
			this.projectilesPerShot = projectilesPerShot;
		}

		public void setDamage(float damage) {
			this.damage = damage;
		}
		
		public AmmoType build() {
			if (name == null || name.isEmpty())
				throw new IllegalStateException("name is not set");
			if (modelFile == null || modelFile.isEmpty())
				throw new IllegalStateException("modelFile is not set");
			if (textureFile == null || textureFile.isEmpty())
				throw new IllegalStateException("textureFile is not set");
			
			return new AmmoType(this);
		}
	}
	
}
