package com.elytradev.glasshearts.world;

import java.util.Map;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.NBTHelper;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.google.common.collect.Maps;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

public class GlassHeartWorldData extends WorldSavedData {

	private static final NBTHelper nh = new NBTHelper();
	
	private World world;
	
	private Map<BlockPos, GlassHeartData> hearts = Maps.newHashMap();
	
	public GlassHeartWorldData(String name) {
		super(name);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		for (GlassHeartData ghd : nh.deserialize(() -> new GlassHeartData(this), nbt.getTagList("Hearts", NBT.TAG_COMPOUND))) {
			hearts.put(ghd.getHeartPos(), ghd);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag("Hearts", nh.serialize(hearts.values()));
		return nbt;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public GlassHeartData create(BlockPos pos, EnumGlassColor color, ItemStack gemStack, int lifeforce, int lifeforceBuffer) {
		GlassHeartData ghd = new GlassHeartData(this, pos, color, gemStack, lifeforce, lifeforceBuffer);
		hearts.put(pos, ghd);
		markDirty();
		return ghd;
	}
	
	public GlassHeartData get(BlockPos pos) {
		return hearts.get(pos);
	}
	
	public void remove(BlockPos pos) {
		hearts.remove(pos);
		markDirty();
	}
	
	public Iterable<GlassHeartData> all() {
		return hearts.values();
	}
	
	public static GlassHeartWorldData getDataFor(World w) {
		if (w.isRemote) {
			GlassHearts.LOG.warn("getDataFor called on the client-side");
		}
		GlassHeartWorldData data = (GlassHeartWorldData)w.getPerWorldStorage().getOrLoadData(GlassHeartWorldData.class, "glass_hearts");
		if (data == null) {
			data = new GlassHeartWorldData("glass_hearts");
			w.getPerWorldStorage().setData("glass_hearts", data);
		}
		data.setWorld(w);
		return data;
	}

}
