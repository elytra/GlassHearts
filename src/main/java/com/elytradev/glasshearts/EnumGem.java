package com.elytradev.glasshearts;

import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;

import com.elytradev.glasshearts.item.ItemGem;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public enum EnumGem implements IStringSerializable {
	NONE {
		@Override
		public ItemStack toItemStack() {
			return ItemStack.EMPTY;
		}
	},
	
	/**
	 * Poison does 2/3 damage while this container is not empty, and poison
	 * damage to this heart does 1/3 damage. 
	 */
	EMERALD {
		@Override
		public ItemStack toItemStack() {
			return new ItemStack(Items.EMERALD);
		}
	},
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
	DIAMOND {
		@Override
		public ItemStack toItemStack() {
			return new ItemStack(Items.DIAMOND);
		}
	},
	/**
	 * Gives you Absorption when the container is emptied.
	 */
	TOPAZ,
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
	 * Wither does 2/3 damage while this container is not empty, and wither
	 * damage to this heart does 1/3 damage.
	 */
	ONYX,
	/**
	 * 
	 */
	AMBER,
	;
	
	private final String name;
	public final String oreDictionary;
	
	private ItemStack renderingSingleton;
	
	private EnumGem() {
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
