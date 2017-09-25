package com.elytradev.glasshearts.logic;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;
import com.elytradev.glasshearts.world.GlassHeartWorldData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class BlockHeartContainerOwner extends HeartContainerOwner {

	private World world;
	private BlockPos pos;
	
	public BlockHeartContainerOwner() {
	}
	
	public BlockHeartContainerOwner(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}
	
	public IGlassHeart getGlassHeart() {
		if (pos == null) return null;
		if (world == null) return null;
		if (world.isBlockLoaded(pos)) {
			// so this can sometimes work on clients
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityGlassHeart) {
				return (TileEntityGlassHeart)te;
			} else {
				return null;
			}
		}
		GlassHeartWorldData ghwd = GlassHeartWorldData.getDataFor(world);
		return ghwd.get(pos);
	}
	
	@Override
	public void modify(float change) {
		IGlassHeart igh = getGlassHeart();
		if (igh != null) {
			int mb = MathHelper.ceil(igh.getLifeforceCapacity()*change);
			igh.setLifeforce(igh.getLifeforce()+mb);
			for (EntityPlayer ep : GlassHearts.getAllOnlineAttunedPlayers(igh)) {
				ep.addStat(GlassHearts.inst.LIFEFORCE_CONSUMED, mb);
			}
		}
	}
	
	@Override
	public void set(float amount) {
		IGlassHeart igh = getGlassHeart();
		if (igh != null) {
			igh.setLifeforce(MathHelper.ceil(igh.getLifeforceCapacity()*amount));
		}
	}
	
	@Override
	public HeartContainer sync(HeartContainer hc) {
		IGlassHeart igh = getGlassHeart();
		float fill = (igh.getLifeforce()/((float)igh.getLifeforceCapacity()));
		int amt = (int)(fill*255);
		if (hc.getFillAmountInt() != amt || hc.getGem() != igh.getGem()) {
			hc = hc.copy();
			hc.setFillAmountInt(amt);
			hc.setGem(igh.getGem());
		}
		return hc;
	}
	
	@Override
	public boolean isValid() {
		return getGlassHeart() != null;
	}

	public BlockPos getPos() {
		return pos;
	}
	
	public World getWorld() {
		return world;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("Pos", pos.toLong());
		tag.setInteger("Dim", world.provider.getDimension());
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		pos = BlockPos.fromLong(nbt.getLong("Pos"));
		world = DimensionManager.getWorld(nbt.getInteger("Dim"));
	}

	@Override
	public HeartContainerOwner copy() {
		return new BlockHeartContainerOwner(world, pos);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + ((world == null) ? 0 : world.hashCode());
		return result;
	}
	
	@Override
	public String toString() {
		return "block @ "+getPos().getX()+", "+getPos().getY()+", "+getPos().getZ()+" (DIM"+getWorld().provider.getDimension()+")";
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null) {
			return false;
		}
		if (getClass() != that.getClass()) {
			return false;
		}
		BlockHeartContainerOwner other = (BlockHeartContainerOwner) that;
		if (pos == null) {
			if (other.pos != null) {
				return false;
			}
		} else if (!pos.equals(other.pos)) {
			return false;
		}
		if (world == null) {
			if (other.world != null) {
				return false;
			}
		} else if (!world.equals(other.world)) {
			return false;
		}
		return true;
	}
	
	

}
