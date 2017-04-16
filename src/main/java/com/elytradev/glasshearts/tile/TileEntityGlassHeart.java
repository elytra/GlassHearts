package com.elytradev.glasshearts.tile;

import com.elytradev.glasshearts.EnumGem;
import com.elytradev.glasshearts.EnumGlassColor;
import com.elytradev.glasshearts.GlassHeartData;
import com.elytradev.glasshearts.GlassHeartWorldData;
import com.elytradev.glasshearts.GlassHearts;
import com.google.common.base.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntityGlassHeart extends TileEntity implements IFluidHandler {
	
	private boolean useReceivedData = false;
	
	private int receivedLifeforce;
	private int receivedLifeforceBuffer;
	private EnumGlassColor receivedColor;
	private EnumGem receivedGem;
	
	private String name;
	
	@Override
	public ITextComponent getDisplayName() {
		return name == null ? null : new TextComponentString(name);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		tag.setShort("Lifeforce", (short)getLifeforce());
		tag.setShort("LifeforceBuffer", (short)getLifeforceBuffer());
		tag.setByte("Color", (byte)getColor().ordinal());
		tag.setByte("Gem", (byte)getGem().ordinal());
		if (name != null) {
			tag.setString("Name", name);
		}
		return tag;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("CustomName", NBT.TAG_STRING)) {
			name = compound.getString("CustomName");
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		if (name != null) {
			compound.setString("CustomName", name);
		}
		return compound;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		useReceivedData = true;
		receivedLifeforce = tag.getInteger("Lifeforce");
		receivedLifeforceBuffer = tag.getInteger("LifeforceBuffer");
		receivedColor = EnumGlassColor.values()[tag.getByte("Color")];
		receivedGem = EnumGem.values()[tag.getByte("Gem")];
		name = tag.hasKey("Name", NBT.TAG_STRING) ? tag.getString("Name") : null;
	}
	
	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1 || pass == 0;
	}
	
	@Override
	public boolean canRenderBreaking() {
		return true;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T)this;
		}
		return super.getCapability(capability, facing);
	}

	public Optional<GlassHeartData> getData() {
		return hasWorld() && !world.isRemote ? Optional.fromNullable(GlassHeartWorldData.getDataFor(world).get(getPos())) : Optional.absent();
	}
	
	public GlassHeartData getOrCreateData() {
		Optional<GlassHeartData> cur = getData();
		if (cur.isPresent()) {
			return cur.get();
		}
		GlassHeartWorldData ghwd = GlassHeartWorldData.getDataFor(world);
		return ghwd.create(getPos(), EnumGlassColor.NONE, EnumGem.NONE, 0, 0);
	}
	
	public String getName() {
		return name;
	}
	
	public int getLifeforce() {
		if (useReceivedData) {
			return receivedLifeforce;
		}
		return getData().transform(GlassHeartData::getLifeforce).or(0);
	}
	
	public EnumGlassColor getColor() {
		if (useReceivedData) {
			return receivedColor;
		}
		return getData().transform(GlassHeartData::getColor).or(EnumGlassColor.NONE);
	}
	
	public EnumGem getGem() {
		if (useReceivedData) {
			return receivedGem;
		}
		return getData().transform(GlassHeartData::getGem).or(EnumGem.NONE);
	}
	
	public int getLifeforceBuffer() {
		if (useReceivedData) {
			return receivedLifeforceBuffer;
		}
		return getData().transform(GlassHeartData::getLifeforceBuffer).or(0);
	}
	
	public void setName(String name) {
		this.name = name;
		GlassHearts.sendUpdatePacket(this);
	}
	
	public void setLifeforce(int lifeforce) {
		if (!hasWorld() || getWorld().isRemote) return;
		getOrCreateData().setLifeforce(lifeforce);
		GlassHearts.sendUpdatePacket(this);
	}
	
	public void setColor(EnumGlassColor color) {
		if (!hasWorld() || getWorld().isRemote) return;
		getOrCreateData().setColor(color);
		GlassHearts.sendUpdatePacket(this);
	}
	
	public void setGem(EnumGem gem) {
		if (!hasWorld() || getWorld().isRemote) return;
		getOrCreateData().setGem(gem);
		GlassHearts.sendUpdatePacket(this);
	}
	
	public void setLifeforceBuffer(int lifeforceBuffer) {
		if (!hasWorld() || getWorld().isRemote) return;
		getOrCreateData().setLifeforceBuffer(lifeforceBuffer);
		GlassHearts.sendUpdatePacket(this);
	}
	
	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] {
			new FluidTankProperties(new FluidStack(GlassHearts.inst.LIFEFORCE, getLifeforceBuffer()), GlassHearts.inst.configGlassHeartCapacity-getLifeforce(), true, false),
			new FluidTankProperties(new FluidStack(GlassHearts.inst.LIFEFORCE, getLifeforce()), GlassHearts.inst.configGlassHeartCapacity, false, false)
		};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (resource != null && resource.getFluid() == GlassHearts.inst.LIFEFORCE) {
			int max = (GlassHearts.inst.configGlassHeartCapacity-getLifeforce()) - getLifeforceBuffer();
			int amt = Math.min(resource.amount, max);
			if (doFill) {
				setLifeforceBuffer(getLifeforceBuffer()+amt);
			}
			return amt;
		}
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (resource != null && resource.getFluid() == GlassHearts.inst.LIFEFORCE) {
			return drain(resource.amount, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		int amt = Math.min(getLifeforceBuffer(), maxDrain);
		if (doDrain) {
			setLifeforceBuffer(getLifeforceBuffer()-amt);
		}
		return amt < 0 ? null : new FluidStack(GlassHearts.inst.LIFEFORCE, amt);
	}
	
}
