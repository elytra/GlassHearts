package com.elytradev.glasshearts.logic;

import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGlassColor;

import net.minecraft.util.math.BlockPos;

public interface IGlassHeart {
	int getLifeforce();
	int getLifeforceBuffer();
	EnumGem getGem();
	EnumGlassColor getColor();
	BlockPos getPos();
	boolean hasBeenFull();
	
	void setLifeforce(int lifeforce);
	void setLifeforceBuffer(int lifeforceBuffer);
	void setGem(EnumGem gem);
	void setColor(EnumGlassColor color);
	void setHasBeenFull(boolean hasBeenFull);
}
