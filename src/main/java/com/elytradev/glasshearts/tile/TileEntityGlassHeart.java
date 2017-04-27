package com.elytradev.glasshearts.tile;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.elytradev.glasshearts.world.GlassHeartData;
import com.elytradev.glasshearts.world.GlassHeartWorldData;
import com.google.common.base.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityGlassHeart extends TileEntity implements IFluidHandler, IGlassHeart, IItemHandler {
	
	private boolean client = false;
	
	private int clientLifeforce = 0;
	private int clientLifeforceCapacity = 0;
	private int clientLifeforceBuffer = 0;
	private EnumGlassColor clientColor = EnumGlassColor.NONE;
	private EnumGem clientGem = EnumGem.NONE;
	private boolean clientHasBeenFull = false;
	
	private String name;
	
	@Override
	public void onLoad() {
		super.onLoad();
		GlassHearts.proxy.onLoad(this);
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return name == null ? null : new TextComponentString(name);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		tag.setShort("Lifeforce", (short)getLifeforce());
		tag.setShort("LifeforceCapacity", (short)getLifeforceCapacity());
		tag.setShort("LifeforceBuffer", (short)getLifeforceBuffer());
		tag.setByte("Color", (byte)getColor().ordinal());
		tag.setByte("Gem", (byte)getGem().ordinal());
		tag.setBoolean("HasBeenFull", hasBeenFull());
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
		return new SPacketUpdateTileEntity(getHeartPos(), 0, getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		client = true;
		clientLifeforce = tag.getInteger("Lifeforce");
		clientLifeforceBuffer = tag.getInteger("LifeforceBuffer");
		clientLifeforceCapacity = tag.getInteger("LifeforceCapacity");
		clientColor = EnumGlassColor.values()[tag.getByte("Color")];
		clientGem = EnumGem.values()[tag.getByte("Gem")];
		clientHasBeenFull = tag.getBoolean("HasBeenFull");
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
		} else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T)this;
		} else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T)this;
		}
		return super.getCapability(capability, facing);
	}

	public Optional<GlassHeartData> getData() {
		return hasWorld() && !world.isRemote ? Optional.fromNullable(GlassHeartWorldData.getDataFor(world).get(getHeartPos())) : Optional.absent();
	}
	
	public GlassHeartData getOrCreateData() {
		Optional<GlassHeartData> cur = getData();
		if (cur.isPresent()) {
			return cur.get();
		}
		GlassHeartWorldData ghwd = GlassHeartWorldData.getDataFor(world);
		return ghwd.create(getHeartPos(), EnumGlassColor.NONE, EnumGem.NONE, 0, 0);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int getLifeforce() {
		if (client) return clientLifeforce;
		return getData().transform(GlassHeartData::getLifeforce).or(0);
	}
	
	@Override
	public int getLifeforceCapacity() {
		if (client) return clientLifeforceCapacity;
		return getData().transform(GlassHeartData::getLifeforceCapacity).or(0);
	}
	
	@Override
	public EnumGlassColor getColor() {
		if (client) return clientColor;
		return getData().transform(GlassHeartData::getColor).or(EnumGlassColor.NONE);
	}
	
	@Override
	public EnumGem getGem() {
		if (client) return clientGem;
		return getData().transform(GlassHeartData::getGem).or(EnumGem.NONE);
	}
	
	@Override
	public int getLifeforceBuffer() {
		if (client) return clientLifeforceBuffer;
		return getData().transform(GlassHeartData::getLifeforceBuffer).or(0);
	}
	
	@Override
	public boolean hasBeenFull() {
		if (client) return clientHasBeenFull;
		return getData().transform(GlassHeartData::hasBeenFull).or(false);
	}
	
	public void setName(String name) {
		this.name = name;
		GlassHearts.sendUpdatePacket(this);
		markDirty();
	}
	
	@Override
	public void setLifeforce(int lifeforce) {
		if (client) this.clientLifeforce = lifeforce;
		if (!hasWorld() || getWorld().isRemote) return;
		int oldLifeforce = getLifeforce();
		getOrCreateData().setLifeforce(lifeforce);
		if (oldLifeforce != lifeforce) {
			GlassHearts.sendUpdatePacket(this);
			getWorld().updateComparatorOutputLevel(getPos(), getBlockType());
		}
	}
	
	@Override
	public void setColor(EnumGlassColor color) {
		if (client) this.clientColor = color;
		if (!hasWorld() || getWorld().isRemote) return;
		EnumGlassColor oldColor = getColor();
		getOrCreateData().setColor(color);
		if (oldColor != color) {
			GlassHearts.sendUpdatePacket(this);
		}
	}
	
	@Override
	public void setGem(EnumGem gem) {
		if (client) this.clientGem = gem;
		if (!hasWorld() || getWorld().isRemote) return;
		EnumGem oldGem = getGem();
		getOrCreateData().setGem(gem);
		if (oldGem != gem) {
			GlassHearts.sendUpdatePacket(this);
		}
	}
	
	@Override
	public void setLifeforceBuffer(int lifeforceBuffer) {
		if (client) this.clientLifeforceBuffer = lifeforceBuffer;
		if (!hasWorld() || getWorld().isRemote) return;
		int oldLifeforceBuffer = getLifeforceBuffer();
		getOrCreateData().setLifeforceBuffer(lifeforceBuffer);
		if (oldLifeforceBuffer != lifeforceBuffer) {
			GlassHearts.sendUpdatePacket(this);
			getWorld().updateComparatorOutputLevel(getPos(), getBlockType());
		}
	}
	
	@Override
	public void setHasBeenFull(boolean hasBeenFull) {
		if (client) this.clientHasBeenFull = hasBeenFull;
		if (!hasWorld() || getWorld().isRemote) return;
		boolean oldHasBeenFull = hasBeenFull();
		getOrCreateData().setHasBeenFull(hasBeenFull);
		if (oldHasBeenFull != hasBeenFull) {
			GlassHearts.sendUpdatePacket(this);
		}
	}
	
	@Override
	public BlockPos getHeartPos() {
		return getPos();
	}
	
	@Override
	public World getHeartWorld() {
		return getWorld();
	}
	
	public void setClient(boolean client) {
		this.client = client;
	}
	
	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] {
			new FluidTankProperties(new FluidStack(GlassHearts.inst.LIFEFORCE, getLifeforceBuffer()), getLifeforceCapacity()-getLifeforce(), true, false),
			new FluidTankProperties(new FluidStack(GlassHearts.inst.LIFEFORCE, getLifeforce()), getLifeforceCapacity(), false, false)
		};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (resource != null && resource.getFluid() == GlassHearts.inst.LIFEFORCE) {
			int max = (getLifeforceCapacity()-getLifeforce()) - getLifeforceBuffer();
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

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot != 0) return null;
		return getGem().toItemStack();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack == null) return null;
		if (slot != 0) return stack;
		if (getGem() == EnumGem.NONE) {
			EnumGem eg = EnumGem.fromItemStack(stack);
			if (eg != null) {
				ItemStack is = stack.copy();
				is.stackSize--;
				if (!simulate) {
					setGem(eg);
				}
				return is;
			}
		}
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (slot != 0) return null;
		EnumGem gem = getGem();
		if (!simulate) {
			setGem(EnumGem.NONE);
		}
		return gem.toItemStack();
	}
	
}
