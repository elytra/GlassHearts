package com.elytradev.glasshearts.logic;

import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.gem.Gem;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IGlassHeart {
	int getLifeforce();
	int getLifeforceBuffer();
	int getLifeforceCapacity();
	Gem getGem();
	ItemStack getGemStack();
	EnumGlassColor getColor();
	BlockPos getHeartPos();
	World getHeartWorld();
	boolean hasBeenFull();
	
	void setLifeforce(int lifeforce);
	void setLifeforceBuffer(int lifeforceBuffer);
	void setGemStack(ItemStack gem);
	void setColor(EnumGlassColor color);
	void setHasBeenFull(boolean hasBeenFull);
}
