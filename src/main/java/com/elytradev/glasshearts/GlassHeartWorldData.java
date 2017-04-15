package com.elytradev.glasshearts;


import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.elytradev.concrete.NBTHelper;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

public class GlassHeartWorldData extends WorldSavedData {

	private static final NBTHelper nh = new NBTHelper();
	
	private Map<BlockPos, GlassHeartData> hearts = Maps.newHashMap();
	
	public GlassHeartWorldData(String name) {
		super(name);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		for (GlassHeartData ghd : nh.deserialize(() -> new GlassHeartData(this), nbt.getTagList("Hearts", NBT.TAG_COMPOUND))) {
			hearts.put(ghd.getPos(), ghd);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag("Hearts", nh.serialize(hearts.values()));
		return nbt;
	}
	
	public GlassHeartData create(BlockPos pos, EnumGlassColor color, EnumGem gem, int lifeforce) {
		GlassHeartData ghd = new GlassHeartData(this, pos, color, gem, lifeforce);
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
	
	public static GlassHeartWorldData getDataFor(World w) {
		if (w.isRemote) {
			LogManager.getLogger("GlassHearts").warn("getDataFor called on the client-side");
		}
		GlassHeartWorldData data = (GlassHeartWorldData)w.getPerWorldStorage().getOrLoadData(GlassHeartWorldData.class, "glass_hearts");
		if (data == null) {
			data = new GlassHeartWorldData("glass_hearts");
			w.getPerWorldStorage().setData("glass_hearts", data);
		}
		return data;
	}

}
