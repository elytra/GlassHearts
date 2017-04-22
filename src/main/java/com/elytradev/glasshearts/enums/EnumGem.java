package com.elytradev.glasshearts.enums;

import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.item.ItemGem;
import com.elytradev.glasshearts.logic.IGlassHeart;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public enum EnumGem implements IStringSerializable {
	NONE(0) {
		@Override
		public ItemStack toItemStack() {
			return ItemStack.EMPTY;
		}
	},
	
	/**
	 * Poison damage to this heart is 80% less effective.
	 */
	EMERALD(0x56BB73) {
		@Override
		public ItemStack toItemStack() {
			return new ItemStack(Items.EMERALD);
		}
	},
	/**
	 * Gives you Regeneration when the container is emptied. Note that
	 * Regeneration does not work on glass hearts.
	 */
	AMETHYST(0x5A399B),
	/**
	 * Container immediately refills after being emptied, but the gem shatters.
	 */
	RUBY(0x874446) {
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.hasBeenFull() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
	},
	/**
	 * If this container is full, your armor is 2.5% more effective for every
	 * heart before this one. If it's empty, your armor is 20% less effective.
	 */
	DIAMOND(0x81C2B5) {
		@Override
		public ItemStack toItemStack() {
			return new ItemStack(Items.DIAMOND);
		}
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			if (igh.getLifeforce() <= 0) {
				return EnumGemState.ACTIVE_CURSED;
			}
			if (igh.getLifeforce() == GlassHearts.inst.configGlassHeartCapacity) {
				return EnumGemState.ACTIVE_BENEFICIAL;
			}
			return EnumGemState.INACTIVE;
		}
	},
	/**
	 * Gives you Absorption when the container is emptied.
	 */
	TOPAZ(0xB27639) {
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.getLifeforce() <= 0 ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
	},
	/**
	 * If this container is at least half full, damage dealt will be capped to
	 * this container only.
	 */
	SAPPHIRE(0x3A4780),
	/**
	 * Container very slowly refills.
	 */
	OPAL(0x94B1AC) {
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.getLifeforceBuffer() <= 0 && igh.getLifeforce() < GlassHearts.inst.configGlassHeartCapacity ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
	},
	/**
	 * Wither damage to this heart is 80% less effective.
	 */
	ONYX(0x434343),
	/**
	 * All damage to this heart is 50% less effective.
	 */
	AMBER(0xC2983A),
	;
	
	private final String name;
	public final String oreDictionary;
	public final int color;
	
	private ItemStack renderingSingleton;
	
	private EnumGem(int color) {
		this.color = color;
		this.name = name().toLowerCase(Locale.ROOT);
		oreDictionary = "gem"+Character.toString(name().charAt(0))+this.name.substring(1);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public ItemStack getRenderingSingleton() {
		if (renderingSingleton == null) {
			renderingSingleton = toItemStack();
		}
		return renderingSingleton;
	}
	
	public EnumGemState getState(IGlassHeart igh) {
		return igh.getLifeforce() > 0 ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	public ItemStack toItemStack() {
		int idx = ArrayUtils.indexOf(ItemGem.VALID_GEMS, this);
		if (idx == -1) throw new AssertionError("toItemStack not overridden for special gem "+this);
		return new ItemStack(GlassHearts.inst.GEM, 1, idx);
	}
	
	public static EnumGem fromItemStack(ItemStack stack) {
		if (stack.getItem() == Items.DIAMOND) {
			return DIAMOND;
		}
		if (stack.getItem() == Items.EMERALD) {
			return EMERALD;
		}
		if (stack.getItem() == GlassHearts.inst.GEM) {
			return ItemGem.VALID_GEMS[stack.getMetadata()%ItemGem.VALID_GEMS.length];
		}
		return null;
	}
}
