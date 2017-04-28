package com.elytradev.glasshearts.client.guiparticle;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PendingEffect {
	public interface EffectSpawner {
		@SideOnly(Side.CLIENT)
		void spawn(int x, int y);
	}

	private final int index;
	private final EffectSpawner effect;
	public PendingEffect(int index, EffectSpawner effect) {
		this.index = index;
		this.effect = effect;
	}
	
	public int getIndex() {
		return index;
	}
	
	public EffectSpawner getEffect() {
		return effect;
	}
	
}
