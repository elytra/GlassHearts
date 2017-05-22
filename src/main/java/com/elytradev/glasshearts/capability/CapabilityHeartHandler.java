package com.elytradev.glasshearts.capability;

import com.elytradev.glasshearts.logic.HeartContainer;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityHeartHandler {
	@CapabilityInject(IHeartHandler.class)
	public static Capability<IHeartHandler> CAPABILITY;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IHeartHandler.class, new IStorage<IHeartHandler>() {

			@Override
			public NBTBase writeNBT(Capability<IHeartHandler> capability, IHeartHandler instance, EnumFacing side) {
				if (instance instanceof INBTSerializable) {
					return ((INBTSerializable) instance).serializeNBT();
				}
				NBTTagCompound tag = new NBTTagCompound();
				NBTTagList li = new NBTTagList();
				for (int i = 0; i < instance.getContainers(); i++) {
					HeartContainer hc = instance.getContainer(i);
					li.appendTag(hc.serializeNBT());
				}
				tag.setTag("Containers", li);
				return tag;
			}

			@Override
			public void readNBT(Capability<IHeartHandler> capability, IHeartHandler instance, EnumFacing side, NBTBase nbt) {
				if (instance instanceof INBTSerializable) {
					((INBTSerializable) instance).deserializeNBT(nbt);
					return;
				}
				instance.clear();
				NBTTagCompound tag = (NBTTagCompound)nbt;
				NBTTagList li = tag.getTagList("Containers", NBT.TAG_COMPOUND);
				for (int i = 0; i < li.tagCount(); i++) {
					HeartContainer hc = HeartContainer.createFromNBT(li.getCompoundTagAt(i));
					hc.animationTicks = 512;
					instance.addContainer(hc);
				}
			}
			
		}, BasicHeartHandler::new);
	}
}
