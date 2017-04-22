package com.elytradev.glasshearts.enums;

import java.util.Locale;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.IStringSerializable;

public enum EnumGlassColor implements IStringSerializable {
	NONE(null),
	
	WHITE(EnumDyeColor.WHITE),
	ORANGE(EnumDyeColor.ORANGE),
	MAGENTA(EnumDyeColor.MAGENTA),
	LIGHT_BLUE(EnumDyeColor.LIGHT_BLUE),
	YELLOW(EnumDyeColor.YELLOW),
	LIME(EnumDyeColor.LIME),
	PINK(EnumDyeColor.PINK),
	GRAY(EnumDyeColor.GRAY),
	SILVER(EnumDyeColor.SILVER),
	CYAN(EnumDyeColor.CYAN),
	PURPLE(EnumDyeColor.PURPLE),
	BLUE(EnumDyeColor.BLUE),
	BROWN(EnumDyeColor.BROWN),
	GREEN(EnumDyeColor.GREEN),
	RED(EnumDyeColor.RED),
	BLACK(EnumDyeColor.BLACK),
	;
	
	private final String name;
	public final EnumDyeColor dye;
	
	private EnumGlassColor(EnumDyeColor dye) {
		this.dye = dye;
		this.name = name().toLowerCase(Locale.ROOT);
	}
	
	@Override
	public String getName() {
		return name;
	}
}
