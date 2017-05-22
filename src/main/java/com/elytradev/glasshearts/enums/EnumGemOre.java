package com.elytradev.glasshearts.enums;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumGemOre implements IStringSerializable {
	AMETHYST,
	RUBY,
	TOPAZ,
	SAPPHIRE,
	OPAL,
	ONYX,
	AGATE,
	AMBER,
	;
	
	public static final EnumGemOre[] VALUES = values();
	
	private final String name;
	
	private EnumGemOre() {
		this.name = name().toLowerCase(Locale.ROOT);
	}
	
	@Override
	public String getName() {
		return name;
	}
}
