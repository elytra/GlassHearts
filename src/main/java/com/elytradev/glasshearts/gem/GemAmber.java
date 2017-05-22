package com.elytradev.glasshearts.gem;

import net.minecraft.util.DamageSource;

public class GemAmber extends Gem {

	public GemAmber() {
		super(0xC2983A, "gemAmber");
		setDefaultTexture(9);
	}
	
	@Override
	public float getMultiplier(DamageSource src) {
		return 0.5f;
	}
	
}
