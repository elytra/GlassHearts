package com.elytradev.glasshearts.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class HeartContainerOwner implements INBTSerializable<NBTTagCompound> {

	@Override
	public abstract int hashCode();
	@Override
	public abstract boolean equals(Object that);
	
	public abstract HeartContainerOwner copy();
	
	public abstract void modify(float change);
	public abstract void set(float amount);
	
	public abstract HeartContainer sync(HeartContainer hc);
	
	public abstract boolean isValid();
	
}
