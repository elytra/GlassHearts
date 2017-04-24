package com.elytradev.glasshearts.logic;

import java.util.Locale;

import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;
import com.elytradev.glasshearts.world.GlassHeartWorldData;
import com.google.common.base.Enums;
import com.google.common.base.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

public class HeartContainer implements INBTSerializable<NBTTagCompound> {
	private EnumGlassColor glassColor;
	private EnumGem gem;
	private float fillAmount;
	private float lastFillAmount;
	
	private World ownerWorld;
	private BlockPos ownerPos;

	public HeartContainer() {}

	public HeartContainer(EnumGlassColor glassColor, EnumGem gem, float fillAmount, World ownerWorld, BlockPos ownerPos) {
		if (gem == null) throw new IllegalArgumentException("Null gem is invalid, use EnumGem.NONE");
		this.glassColor = glassColor;
		this.gem = gem;
		this.fillAmount = fillAmount;
		this.ownerWorld = ownerWorld;
		this.ownerPos = ownerPos;
	}
	
	// factory methods
	
	public static HeartContainer createNatural(EnumGem gem, float fillAmount) {
		return new HeartContainer(null, gem, fillAmount, null, null);
	}
	
	public static HeartContainer createGlass(EnumGlassColor color, EnumGem gem, float fillAmount) {
		if (color == null) throw new IllegalArgumentException("Null color is not permitted in createGlass, use the constructor directly or createNatural");
		return new HeartContainer(color, gem, fillAmount, null, null);
	}
	
	public static HeartContainer createGlass(IGlassHeart igh) {
		return new HeartContainer(igh.getColor(), igh.getGem(), igh.getLifeforce()/(float)igh.getLifeforceCapacity(), igh.getHeartWorld(), igh.getHeartPos());
	}
	
	
	// intended-to-be-overridden methods
	
	public boolean canHeal() {
		return glassColor == null;
	}
	
	public float damage(float amount, DamageSource src) {
		if (fillAmount <= 0) return 0;
		float mult = gem.getMultiplier(src);
		if (amount*mult > fillAmount && gem.doesBlockDamage(src, this)) {
			fillAmount = 0;
			IGlassHeart igh = getOwner();
			if (igh != null) {
				igh.setLifeforce(0);
			}
			return amount;
		}
		amount *= mult;
		float dmg = Math.min(amount, fillAmount);
		fillAmount -= dmg;
		IGlassHeart igh = getOwner();
		if (igh != null) {
			igh.setLifeforce(MathHelper.ceil(igh.getLifeforce()-(igh.getLifeforceCapacity()*dmg)));
		}
		return dmg / mult;
	}
	
	public float heal(float amount) {
		if (!canHeal()) return 0;
		float heal = Math.min(amount, 1-fillAmount);
		fillAmount += heal;
		IGlassHeart igh = getOwner();
		if (igh != null) {
			igh.setLifeforce(MathHelper.ceil(igh.getLifeforce()+(igh.getLifeforceCapacity()*heal)));
		}
		return heal;
	}
	
	
	
	// basic getters and setters
	
	public float getFillAmount() {
		return fillAmount;
	}
	
	public float getLastFillAmount() {
		return lastFillAmount;
	}
	
	public EnumGem getGem() {
		return gem;
	}
	
	public EnumGlassColor getGlassColor() {
		return glassColor;
	}
	
	public BlockPos getOwnerPos() {
		return ownerPos;
	}
	
	public World getOwnerWorld() {
		return ownerWorld;
	}
	
	
	
	public void setFillAmount(float fillAmount) {
		this.fillAmount = fillAmount;
	}
	
	public void setLastFillAmount(float lastFillAmount) {
		this.lastFillAmount = lastFillAmount;
	}
	
	public void setGem(EnumGem gem) {
		if (gem == null) throw new IllegalArgumentException("Null gem is invalid, use EnumGem.NONE");
		this.gem = gem;
	}
	
	public void setGlassColor(EnumGlassColor glassColor) {
		this.glassColor = glassColor;
	}
	
	public void setOwnerPos(BlockPos ownerPos) {
		this.ownerPos = ownerPos;
	}
	
	public void setOwnerWorld(World ownerWorld) {
		this.ownerWorld = ownerWorld;
	}
	
	public HeartContainer copy() {
		return new HeartContainer(glassColor, gem, fillAmount, ownerWorld, ownerPos);
	}
	
	
	
	// convenience getters and setters
	
	public IGlassHeart getOwner() {
		if (ownerPos == null) return null;
		if (ownerWorld == null) return null;
		if (ownerWorld.isBlockLoaded(ownerPos)) {
			// so this can sometimes work on clients
			TileEntity te = ownerWorld.getTileEntity(ownerPos);
			if (te instanceof TileEntityGlassHeart) {
				return (TileEntityGlassHeart)te;
			} else {
				return null;
			}
		}
		GlassHeartWorldData ghwd = GlassHeartWorldData.getDataFor(ownerWorld);
		return ghwd.get(ownerPos);
	}
	
	public int getFillAmountInt() {
		return ((int)(fillAmount*255f))&0xFF;
	}
	
	public byte getFillAmountByte() {
		return (byte)getFillAmountInt();
	}
	
	public void setFillAmountInt(int fillAmount) {
		setFillAmount((fillAmount&0xFF)/255f);
	}
	
	public void setFillAmountByte(byte fillAmount) {
		setFillAmountInt(fillAmount&0xFF);
	}
	
	
	
	// interface implementations
	
	@Override
	public String toString() {
		String gemStr = (gem == EnumGem.NONE) ? "" : "+"+gem.getName();
		if (glassColor == null) {
			return "natural"+gemStr+"["+fillAmount+"]";
		} else if (glassColor == EnumGlassColor.NONE) {
			return "glass"+gemStr+"["+fillAmount+"]";
		} else {
			return glassColor.getName()+"_glass"+gemStr+"["+fillAmount+"]";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getFillAmountInt();
		result = prime * result + ((gem == null) ? 0 : gem.hashCode());
		result = prime * result + ((glassColor == null) ? 0 : glassColor.hashCode());
		result = prime * result + ((ownerPos == null) ? 0 : ownerPos.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HeartContainer that = (HeartContainer) obj;
		if (this.getFillAmountInt() != that.getFillAmountInt()) {
			return false;
		}
		if (this.gem != that.gem) {
			return false;
		}
		if (this.glassColor != that.glassColor) {
			return false;
		}
		if (!Objects.equal(this.ownerPos, that.ownerPos)) {
			return false;
		}
		return true;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		glassColor = Enums.getIfPresent(EnumGlassColor.class, nbt.getString("Color").toUpperCase(Locale.ROOT)).orNull();
		gem = Enums.getIfPresent(EnumGem.class, nbt.getString("Gem").toUpperCase(Locale.ROOT)).or(EnumGem.NONE);
		setFillAmountByte(nbt.getByte("Fill"));
		lastFillAmount = fillAmount;
		ownerPos = nbt.hasKey("OwnerPos", NBT.TAG_LONG) ? BlockPos.fromLong(nbt.getLong("OwnerPos")) : null;
	}
	
	public static HeartContainer createFromNBT(NBTTagCompound nbt) {
		HeartContainer hc = new HeartContainer(null, EnumGem.NONE, 0, null, null);
		hc.deserializeNBT(nbt);
		return hc;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		if (glassColor != null) {
			tag.setString("Color", glassColor.getName());
		}
		tag.setString("Gem", gem.getName());
		tag.setByte("Fill", getFillAmountByte());
		if (ownerPos != null) {
			tag.setLong("OwnerPos", ownerPos.toLong());
		}
		return tag;
	}
	
	

}
