package com.elytradev.glasshearts;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumGem implements IStringSerializable {
	NONE,
	
	/**
	 * Attacker receives Poison when the container is emptied.
	 */
	EMERALD,
	/**
	 * Gives you Regeneration when the container is emptied. Note that
	 * Regeneration does not work on glass hearts.
	 */
	AMETHYST,
	/**
	 * Container immediately refills after being emptied, but the gem shatters.
	 */
	RUBY,
	/**
	 * If this container is full, your armor is 40% more effective. If it's
	 * empty, your armor is 20% less effective.
	 */
	DIAMOND,
	/**
	 * Gives you Absorption when the container is emptied.
	 */
	AMBER,
	/**
	 * If this container is at least half full, damage dealt will be capped to
	 * this container only.
	 */
	SAPPHIRE,
	/**
	 * Container very slowly refills.
	 */
	OPAL,
	/**
	 * Atacker receives Wither when the container is emptied.
	 */
	ONYX,
	;
	
	private final String name;
	public final String oreDictionary;
	
	private EnumGem() {
		this.name = name().toLowerCase(Locale.ROOT);
		oreDictionary = "gem"+Character.toString(name().charAt(0))+this.name.substring(1);
	}
	
	@Override
	public String getName() {
		return name;
	}
}
