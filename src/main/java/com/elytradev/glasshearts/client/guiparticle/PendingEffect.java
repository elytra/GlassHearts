package com.elytradev.glasshearts.client.guiparticle;

public class PendingEffect {
	public interface EffectSpawner {
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
