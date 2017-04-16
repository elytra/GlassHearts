package com.elytradev.glasshearts;

public class HeartContainer {
	private EnumGlassColor glassColor;
	private EnumGem gem;
	private float fillAmount;

	public HeartContainer() {
	}

	public HeartContainer(EnumGlassColor glassColor, EnumGem gem, float fillAmount) {
		this.glassColor = glassColor;
		this.gem = gem;
		this.fillAmount = fillAmount;
	}
	
	public float getFillAmount() {
		return fillAmount;
	}
	
	public EnumGem getGem() {
		return gem;
	}
	
	public EnumGlassColor getGlassColor() {
		return glassColor;
	}
	
	public void setFillAmount(float fillAmount) {
		this.fillAmount = fillAmount;
	}
	
	public void setGem(EnumGem gem) {
		this.gem = gem;
	}
	
	public void setGlassColor(EnumGlassColor glassColor) {
		this.glassColor = glassColor;
	}

}
