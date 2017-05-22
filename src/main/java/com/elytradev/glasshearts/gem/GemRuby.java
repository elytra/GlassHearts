package com.elytradev.glasshearts.gem;

import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.logic.IGlassHeart;

import net.minecraft.item.ItemStack;

public class GemRuby extends Gem {
	public GemRuby() {
		super(0x874446, "gemRuby");
		setDefaultTexture(3);
	}
	
	@Override
	public EnumGemState getState(IGlassHeart igh) {
		return igh.hasBeenFull() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	@Override
	public void onEmpty(IGlassHeart igh) {
		igh.setGemStack(ItemStack.EMPTY);
		// :^)
		igh.setLifeforce(igh.getLifeforceCapacity()-1);
	}
}
