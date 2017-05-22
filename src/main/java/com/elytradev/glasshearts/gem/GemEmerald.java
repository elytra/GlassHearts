package com.elytradev.glasshearts.gem;

import net.minecraft.util.DamageSource;

public class GemEmerald extends Gem {
	public GemEmerald() {
		super(0x56BB73, "gemEmerald");
		setDefaultTexture(1);
	}
	
	@Override
	public float getMultiplier(DamageSource src) {
		if ("magic".equals(src.damageType)) {
			return 0.2f;
		}
		return 1;
	}
}
