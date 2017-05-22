package com.elytradev.glasshearts.gem;

import net.minecraft.util.DamageSource;

public class GemOnyx extends Gem {

	public GemOnyx() {
		super(0x434343, "gemOnyx");
		setDefaultTexture(8);
	}
	
	@Override
	public float getMultiplier(DamageSource src) {
		if ("wither".equals(src.damageType)) {
			return 0.2f;
		}
		return 1;
	}
	
}
