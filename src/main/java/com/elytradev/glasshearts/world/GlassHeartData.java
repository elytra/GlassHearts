package com.elytradev.glasshearts.world;

import java.util.Locale;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.enums.EnumGemOre;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.gem.Gem;
import com.elytradev.glasshearts.init.Gems;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.google.common.base.Enums;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

public class GlassHeartData implements INBTSerializable<NBTTagCompound>, IGlassHeart {
	
	private GlassHeartWorldData parent;
	
	private BlockPos pos;
	private EnumGlassColor color;
	private ItemStack gemStack;
	private int lifeforce;
	private int lifeforceBuffer;
	private boolean hasBeenFull;
	
	private transient Gem gem = Gems.NONE;
	
	
	public GlassHeartData(GlassHeartWorldData parent) {
		this.parent = parent;
	}
	
	public GlassHeartData(GlassHeartWorldData parent, BlockPos pos, EnumGlassColor color, ItemStack gemStack, int lifeforce, int lifeforceBuffer) {
		this.parent = parent;
		this.pos = pos;
		this.color = color;
		this.gemStack = gemStack;
		this.lifeforce = lifeforce;
		this.lifeforceBuffer = lifeforceBuffer;
		
		this.gem = Gem.fromItemStack(gemStack);
	}
	
	
	@Override
	public EnumGlassColor getColor() {
		return color;
	}
	
	@Override
	public Gem getGem() {
		return gem;
	}
	
	@Override
	public ItemStack getGemStack() {
		return gemStack;
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
	public void setGemStack(ItemStack gemStack) {
		this.gemStack = gemStack;
		this.gem = Gem.fromItemStack(gemStack);
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
		if (nbt.hasKey("Gem", NBT.TAG_STRING)) {
			gem = Gem.REGISTRY.getValue(new ResourceLocation("glasshearts", nbt.getString("Gem").toLowerCase(Locale.ROOT)));
			if (gem == null) gem = Gems.NONE;
			if (gem == Gems.NONE) {
				gemStack = ItemStack.EMPTY;
			} else if (gem == Gems.DIAMOND) {
				gemStack = new ItemStack(Items.DIAMOND, 1, 0);
			} else if (gem == Gems.EMERALD) {
				gemStack = new ItemStack(Items.EMERALD, 1, 0);
			} else {
				gemStack = new ItemStack(GlassHearts.inst.GEM, 1, EnumGemOre.valueOf(gem.getRegistryName().getResourcePath().toUpperCase(Locale.ROOT)).ordinal());
			}
			System.out.println("loaded legacy gem "+gem.getRegistryName()+" ("+nbt.getString("Gem")+") as stack "+gemStack);
		} else {
			gemStack = new ItemStack(nbt.getCompoundTag("GemStack"));
			gem = Gem.fromItemStack(gemStack);
		}
		lifeforce = Math.max(0, Math.min(nbt.getShort("Lifeforce"), getLifeforceCapacity()));
		lifeforceBuffer = Math.max(0, Math.min(nbt.getShort("LifeforceBuffer"), getLifeforceCapacity()-lifeforce));
		hasBeenFull = nbt.getBoolean("HasBeenFull");
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("Pos", pos.toLong());
		tag.setString("Color", color.getName());
		tag.setTag("GemStack", gemStack.serializeNBT());
		tag.setShort("Lifeforce", (short)lifeforce);
		tag.setShort("LifeforceBuffer", (short)lifeforceBuffer);
		tag.setBoolean("HasBeenFull", hasBeenFull);
		return tag;
	}
	
}
