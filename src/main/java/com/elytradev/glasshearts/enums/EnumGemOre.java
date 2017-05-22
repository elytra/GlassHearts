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
	AMBER,
	AGATE,
	;
	
	public static final EnumGemOre[] VALUES = values();
	public static final EnumGemOre[] VALUES_WITHOUT_AMBER = {
			AMETHYST, RUBY, TOPAZ, SAPPHIRE, OPAL, ONYX, AGATE
	};
	
	private final String name;
	
	private EnumGemOre() {
		this.name = name().toLowerCase(Locale.ROOT);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public int ordinalWithoutAmber() {
		if (this == AMBER) throw new UnsupportedOperationException("Cannot get the ordinal-without-amber of amber");
		if (ordinal() >= AMBER.ordinal()) {
			return ordinal()-1;
		}
		return ordinal();
	}
}
