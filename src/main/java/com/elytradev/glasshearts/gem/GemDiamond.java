package com.elytradev.glasshearts.gem;

import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.logic.IGlassHeart;

public class GemDiamond extends Gem {

	public GemDiamond() {
		super(0x81C2B5, "gemDiamond");
		setDefaultTexture(4);
	}
	
	@Override
	public EnumGemState getState(IGlassHeart igh) {
		return igh.getLifeforceBuffer() > 0 ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	@Override
	public int adjustFillRate(int fillRate) {
		return fillRate+(fillRate/2);
	}
	
}
