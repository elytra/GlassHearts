package com.elytradev.glasshearts.world;

import java.util.Locale;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.google.common.base.Enums;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class GlassHeartData implements INBTSerializable<NBTTagCompound>, IGlassHeart {
	
	private GlassHeartWorldData parent;
	
	private BlockPos pos;
	private EnumGlassColor color;
	private EnumGem gem;
	private int lifeforce;
	private int lifeforceBuffer;
	private boolean hasBeenFull;
	
	
	public GlassHeartData(GlassHeartWorldData parent) {
		this.parent = parent;
	}
	
	public GlassHeartData(GlassHeartWorldData parent, BlockPos pos, EnumGlassColor color, EnumGem gem, int lifeforce, int lifeforceBuffer) {
		this.parent = parent;
		this.pos = pos;
		this.color = color;
		this.gem = gem;
		this.lifeforce = lifeforce;
		this.lifeforceBuffer = lifeforceBuffer;
	}
	
	
	@Override
	public EnumGlassColor getColor() {
		return color;
	}
	
	@Override
	public EnumGem getGem() {
		return gem;
	}
	
	@Override
	public int getLifeforce() {
		return lifeforce;
	}
	
	@Override
	public BlockPos getHeartPos() {
		return pos;
	}
	
	@Override
	public int getLifeforceBuffer() {
		return lifeforceBuffer;
	}
	
	@Override
	public int getLifeforceCapacity() {
		return GlassHearts.inst.configGlassHeartCapacity;
	}
	
	@Override
	public boolean hasBeenFull() {
		return hasBeenFull;
	}
	
	@Override
	public World getHeartWorld() {
		return parent.getWorld();
	}
	
	
	@Override
	public void setColor(EnumGlassColor color) {
		this.color = color;
		parent.markDirty();
	}
	
	@Override
	public void setGem(EnumGem gem) {
		this.gem = gem;
		parent.markDirty();
	}
	
	@Override
	public void setLifeforce(int lifeforce) {
		this.lifeforce = lifeforce;
		parent.markDirty();
	}
	
	@Override
	public void setHasBeenFull(boolean hasBeenFull) {
		this.hasBeenFull = hasBeenFull;
		parent.markDirty();
	}
	
	public void setHeartPos(BlockPos pos) {
		this.pos = pos;
		parent.markDirty();
	}
	
	@Override
	public void setLifeforceBuffer(int lifeforceBuffer) {
		this.lifeforceBuffer = lifeforceBuffer;
		parent.markDirty();
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		pos = BlockPos.fromLong(nbt.getLong("Pos"));
		color = Enums.getIfPresent(EnumGlassColor.class, nbt.getString("Color").toUpperCase(Locale.ROOT)).or(EnumGlassColor.NONE);
		gem = Enums.getIfPresent(EnumGem.class, nbt.getString("Gem").toUpperCase(Locale.ROOT)).or(EnumGem.NONE);
		lifeforce = Math.max(0, Math.min(nbt.getShort("Lifeforce"), getLifeforceCapacity()));
		lifeforceBuffer = Math.max(0, Math.min(nbt.getShort("LifeforceBuffer"), getLifeforceCapacity()-lifeforce));
		hasBeenFull = nbt.getBoolean("HasBeenFull");
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("Pos", pos.toLong());
		tag.setString("Color", color.getName());
		tag.setString("Gem", gem.getName());
		tag.setShort("Lifeforce", (short)lifeforce);
		tag.setShort("LifeforceBuffer", (short)lifeforceBuffer);
		tag.setBoolean("HasBeenFull", hasBeenFull);
		return tag;
	}
	
}
