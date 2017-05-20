package com.elytradev.glasshearts.integration.tcon;

import slimeknights.tconstruct.library.TinkerRegistry;

public class TConIntegration {

	public static ModifierSapping SAPPING;
	
	public static void init() {
		TinkerRegistry.registerModifier(SAPPING = new ModifierSapping());
		SAPPING.addItem("gemOpal");
	}

}
