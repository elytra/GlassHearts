package com.elytradev.glasshearts;

public class HeartContainer {
	private EnumGlassColor glassColor;
	private EnumGem gem;
	private int decay;
	private float fillAmount;
	private float lastFillAmount;

	public HeartContainer() {
	}

	public HeartContainer(EnumGlassColor glassColor, EnumGem gem, int decay, float fillAmount) {
		this.glassColor = glassColor;
		this.gem = gem;
		this.decay = decay;
		this.fillAmount = fillAmount;
	}
	
	public float getFillAmount() {
		return fillAmount;
	}
	
	public float getLastFillAmount() {
		return lastFillAmount;
	}
	
	public EnumGem getGem() {
		return gem;
	}
	
	public EnumGlassColor getGlassColor() {
		return glassColor;
	}
	
	public int getDecay() {
		return decay;
	}
	
	
	public void setFillAmount(float fillAmount) {
		this.fillAmount = fillAmount;
	}
	
	public void setLastFillAmount(float lastFillAmount) {
		this.lastFillAmount = lastFillAmount;
	}
	
	public void setGem(EnumGem gem) {
		this.gem = gem;
	}
	
	public void setGlassColor(EnumGlassColor glassColor) {
		this.glassColor = glassColor;
	}
	
	public void setDecay(int decay) {
		this.decay = decay;
	}
	
	public HeartContainer copy() {
		return new HeartContainer(glassColor, gem, decay, fillAmount);
	}
	
	
	
	@Override
	public String toString() {
		return "HeartContainer [glassColor=" + glassColor + ", gem=" + gem
				+ ", decay=" + decay + ", fillAmount=" + fillAmount + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + decay;
		result = prime * result + Float.floatToIntBits(fillAmount);
		result = prime * result + ((gem == null) ? 0 : gem.hashCode());
		result = prime * result
				+ ((glassColor == null) ? 0 : glassColor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HeartContainer other = (HeartContainer) obj;
		if (decay != other.decay) {
			return false;
		}
		if (Float.floatToIntBits(fillAmount) != Float
				.floatToIntBits(other.fillAmount)) {
			return false;
		}
		if (gem != other.gem) {
			return false;
		}
		if (glassColor != other.glassColor) {
			return false;
		}
		return true;
	}
	
	

}
