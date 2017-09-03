package com.elytradev.glasshearts.item;

import com.elytradev.glasshearts.enums.EnumGemOre;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemGem extends Item {
	
	public ItemGem() {
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item.glasshearts.gem."+EnumGemOre.VALUES[stack.getMetadata()%EnumGemOre.VALUES.length].getName();
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
			for (int i = 0; i < EnumGemOre.VALUES.length; i++) {
				subItems.add(new ItemStack(this, 1, i));
			}
		}
	}
	
}
