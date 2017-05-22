package com.elytradev.glasshearts.item;

import com.elytradev.glasshearts.enums.EnumGemOre;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockOre extends ItemBlock {

	public ItemBlockOre(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.glasshearts.ore."+EnumGemOre.VALUES_WITHOUT_AMBER[stack.getMetadata()%EnumGemOre.VALUES_WITHOUT_AMBER.length].getName();
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}

}
