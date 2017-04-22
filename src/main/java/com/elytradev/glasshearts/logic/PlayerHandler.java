package com.elytradev.glasshearts.logic;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.capability.CapabilityHealthHandler;
import com.elytradev.glasshearts.capability.IHealthHandler;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.network.PlayHeartEffectMessage;
import com.elytradev.glasshearts.network.UpdateHeartsMessage;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerHandler implements INBTSerializable<NBTTagCompound> {

	private final WeakReference<EntityPlayer> player;
	
	private final List<HeartContainer> lastContainers = Lists.newArrayList();
	
	public PlayerHandler(EntityPlayer player) {
		this.player = new WeakReference<>(player);
	}
	
	protected EntityPlayer getPlayer() {
		EntityPlayer ep = player.get();
		return ep == null || ep.isDead ? null : ep;
	}
	
	protected IHealthHandler getHealthHandler(Entity entity) {
		if (entity == null) return null;
		if (entity.hasCapability(CapabilityHealthHandler.CAPABILITY, null)) {
			return entity.getCapability(CapabilityHealthHandler.CAPABILITY, null);
		}
		return null;
	}
	
	public void preTick() {
		EntityPlayer player = getPlayer();
		if (player == null) return;
		IHealthHandler ihh = getHealthHandler(player);
		if (ihh == null) return;
		float hp = 0;
		for (int i = 0; i < ihh.getContainers(); i++) {
			HeartContainer hc = ihh.getContainer(i);
			IGlassHeart igh = hc.getOwner();
			if (igh != null) {
				int amt = (int)((igh.getLifeforce()/((float)GlassHearts.inst.configGlassHeartCapacity))*255);
				if (hc.getFillAmountInt() != amt) {
					hc = hc.copy();
					hc.setFillAmountInt(amt);
					ihh.setContainer(i, hc);
				}
			} else if (hc.getOwnerPos() != null) {
				ihh.removeContainer(i);
				new PlayHeartEffectMessage(hc.getGlassColor().ordinal(), i).sendTo(player);
				if (hc.getGem() != EnumGem.NONE) {
					new PlayHeartEffectMessage(hc.getGem().ordinal()+(EnumGlassColor.values().length), i).sendTo(player);
				}
				i--;
			}
		}
		for (HeartContainer hc : ihh) {
			hp += hc.getFillAmount();
		}
		player.setHealth(hp*2);
	}
	
	public void postTick() {
		EntityPlayer player = getPlayer();
		if (player == null) return;
		resync();
	}
	
	
	
	public void resync() {
		EntityPlayer player = getPlayer();
		if (player == null) return;
		IHealthHandler ihh = getHealthHandler(player);
		if (ihh == null) return;
		if (lastContainers.isEmpty()) {
			for (HeartContainer hc : ihh) {
				lastContainers.add(hc);
			}
			new UpdateHeartsMessage(0, true, Lists.newArrayList(ihh)).sendTo(player);
		} else {
			int start = -1;
			List<HeartContainer> sync = Collections.emptyList();
			int lastChanged = -1;
			for (int i = 0; i < Math.max(lastContainers.size(), ihh.getContainers()); i++) {
				HeartContainer last = (i >= lastContainers.size()) ? null : lastContainers.get(i);
				HeartContainer cur = (i >= ihh.getContainers()) ? null : ihh.getContainer(i);
				if (start == -1) {
					if (!Objects.equal(last, cur)) {
						start = i;
						sync = Lists.newArrayList(cur);
						lastChanged = i;
					}
				} else {
					sync.add(cur);
					if (!Objects.equal(last, cur)) {
						lastChanged = i;
					}
				}
			}
			if (start != -1) {
				lastContainers.clear();
				for (HeartContainer hc : ihh) {
					lastContainers.add(hc);
				}
				List<HeartContainer> sub = sync.subList(0, (lastChanged-start)+1);
				new UpdateHeartsMessage(start, false, sub).sendTo(player);
			}
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}
	
}
