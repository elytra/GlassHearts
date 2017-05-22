package com.elytradev.glasshearts.gem;

import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.logic.IGlassHeart;

public class GemOpal extends Gem {
	public GemOpal() {
		super(0x94B1AC, "gemOpal");
		setDefaultTexture(7);
	}
	
	
	@Override
	public EnumGemState getState(IGlassHeart igh) {
		return igh.getLifeforceBuffer() <= 0 && igh.getLifeforce() < igh.getLifeforceCapacity() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	@Override
	public void update(IGlassHeart igh, long ticks) {
		if (ticks%10 == 0) {
			if (igh.getLifeforce() < igh.getLifeforceCapacity()) {
				igh.setLifeforce(igh.getLifeforce()+1);
			}
		}
	}
	
	@Override
	public int adjustFillRate(int fillRate) {
		return fillRate/2+(fillRate/4);
	}
}
