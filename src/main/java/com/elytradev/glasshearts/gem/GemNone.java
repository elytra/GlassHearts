package com.elytradev.glasshearts.gem;

import net.minecraft.item.ItemStack;

/**
 * Gem equivalent of BlockAir.
 */
public class GemNone extends Gem {

	public GemNone() {
		super(0, null);
		setDefaultTexture(0);
	}
	
	@Override
	public boolean itemStackMatches(ItemStack stack) {
		return stack == null;
	}

}
