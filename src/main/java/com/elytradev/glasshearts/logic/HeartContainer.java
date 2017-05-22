package com.elytradev.glasshearts.logic;

import java.util.Locale;

import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.gem.Gem;
import com.elytradev.glasshearts.init.Gems;
import com.google.common.base.Enums;
import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

public class HeartContainer implements INBTSerializable<NBTTagCompound> {
	public static final BiMap<String, Class<? extends HeartContainerOwner>> REGISTRY = HashBiMap.create();
	
	static {
		REGISTRY.put("block", BlockHeartContainerOwner.class);
	}
	
	private EnumGlassColor glassColor;
	private Gem gem;
	private float fillAmount;
	private float lastFillAmount;
	
	private HeartContainerOwner owner;
	public int animationTicks;

	public HeartContainer() {}

	public HeartContainer(EnumGlassColor glassColor, Gem gem, float fillAmount, HeartContainerOwner owner) {
		if (gem == null) throw new IllegalArgumentException("Null gem is invalid, use Gems.NONE");
		this.glassColor = glassColor;
		this.gem = gem;
		this.fillAmount = fillAmount;
		this.owner = owner;
	}
	
	// factory methods
	
	public static HeartContainer createNatural(Gem gem, float fillAmount) {
		return new HeartContainer(null, gem, fillAmount, null);
	}
	
	public static HeartContainer createGlass(EnumGlassColor color, Gem gem, float fillAmount) {
		if (color == null) throw new IllegalArgumentException("Null color is not permitted in createGlass, use the constructor directly or createNatural");
		return new HeartContainer(color, gem, fillAmount, null);
	}
	
	public static HeartContainer createGlass(IGlassHeart igh) {
		return new HeartContainer(igh.getColor(), igh.getGem(), igh.getLifeforce()/(float)igh.getLifeforceCapacity(), new BlockHeartContainerOwner(igh.getHeartWorld(), igh.getHeartPos()));
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
			if (hasOwner()) {
				getOwner().set(0);
			}
			return amount;
		}
		amount *= mult;
		float dmg = Math.min(amount, fillAmount);
		fillAmount -= dmg;
		if (hasOwner()) {
			getOwner().modify(-dmg);
		}
		return dmg / mult;
	}
	
	public float heal(float amount) {
		if (!canHeal()) return 0;
		float heal = Math.min(amount, 1-fillAmount);
		fillAmount += heal;
		if (hasOwner()) {
			getOwner().modify(heal);
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
	
	public Gem getGem() {
		return gem;
	}
	
	public EnumGlassColor getGlassColor() {
		return glassColor;
	}
	
	public HeartContainerOwner getOwner() {
		return owner;
	}
	
	
	
	public void setFillAmount(float fillAmount) {
		this.fillAmount = fillAmount;
	}
	
	public void setLastFillAmount(float lastFillAmount) {
		this.lastFillAmount = lastFillAmount;
	}
	
	public void setGem(Gem gem) {
		if (gem == null) throw new IllegalArgumentException("Null gem is invalid, use Gems.NONE");
		this.gem = gem;
	}
	
	public void setGlassColor(EnumGlassColor glassColor) {
		this.glassColor = glassColor;
	}
	
	public void setOwner(HeartContainerOwner owner) {
		this.owner = owner;
	}
	
	public HeartContainer copy() {
		return new HeartContainer(glassColor, gem, fillAmount, owner == null ? null : owner.copy());
	}
	
	
	
	// convenience getters and setters
	
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
	
	public boolean hasOwner() {
		return owner != null;
	}
	
	
	
	// interface implementations
	
	@Override
	public String toString() {
		String gemStr = (gem == Gems.NONE) ? "" : "+"+gem.getRegistryName().getResourcePath();
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
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
		if (!Objects.equal(this.owner, that.owner)) {
			return false;
		}
		return true;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		glassColor = Enums.getIfPresent(EnumGlassColor.class, nbt.getString("Color").toUpperCase(Locale.ROOT)).orNull();
		String gemStr = nbt.getString("Gem");
		if (!gemStr.contains(":")) {
			gemStr = "glasshearts:"+gemStr;
		}
		gem = Gem.REGISTRY.getValue(new ResourceLocation(gemStr));
		setFillAmountByte(nbt.getByte("Fill"));
		lastFillAmount = fillAmount;
		if (nbt.hasKey("Owner", NBT.TAG_COMPOUND)) {
			NBTTagCompound ownerTag = nbt.getCompoundTag("Owner");
			try {
				HeartContainerOwner ihco = REGISTRY.get(ownerTag.getString("Kind")).newInstance();
				ihco.deserializeNBT(ownerTag);
			} catch (Exception e) {
				e.printStackTrace();
				owner = null;
			}
		} else {
			owner = null;
		}
	}
	
	public static HeartContainer createFromNBT(NBTTagCompound nbt) {
		HeartContainer hc = new HeartContainer(null, Gems.NONE, 0, null);
		hc.deserializeNBT(nbt);
		return hc;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		if (glassColor != null) {
			tag.setString("Color", glassColor.getName());
		}
		tag.setString("Gem", gem.getRegistryName().toString());
		tag.setByte("Fill", getFillAmountByte());
		if (owner != null) {
			NBTTagCompound ownerTag = owner.serializeNBT();
			ownerTag.setString("Kind", REGISTRY.inverse().get(owner.getClass()));
			tag.setTag("Owner", ownerTag);
		}
		return tag;
	}
	

}
