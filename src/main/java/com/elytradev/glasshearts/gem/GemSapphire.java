package com.elytradev.glasshearts.gem;

import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.logic.HeartContainer;
import com.elytradev.glasshearts.logic.IGlassHeart;

import net.minecraft.util.DamageSource;

public class GemSapphire extends Gem {
	public GemSapphire() {
		super(0x3A4780, "gemSapphire");
		setDefaultTexture(6);
	}
	
	@Override
	public EnumGemState getState(IGlassHeart igh) {
		return igh.getLifeforce() > (igh.getLifeforceCapacity()/2) ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	@Override
	public boolean doesBlockDamage(DamageSource src, HeartContainer hc) {
		return src.isDamageAbsolute() ? false : hc.getFillAmount() >= 0.5f;
	}
}
