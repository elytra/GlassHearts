package com.elytradev.glasshearts.logic;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import com.elytradev.glasshearts.capability.CapabilityHeartHandler;
import com.elytradev.glasshearts.capability.IHeartHandler;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.network.PlayHeartEffectMessage;
import com.elytradev.glasshearts.network.UpdateHeartsMessage;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerHandler {

	private final WeakReference<EntityPlayer> player;
	
	private final List<HeartContainer> lastContainers = Lists.newArrayList();
	
	public PlayerHandler(EntityPlayer player) {
		this.player = new WeakReference<>(player);
	}
	
	protected EntityPlayer getPlayer() {
		EntityPlayer ep = player.get();
		return ep == null || ep.isDead ? null : ep;
	}
	
	protected IHeartHandler getHealthHandler(Entity entity) {
		if (entity == null) return null;
		if (entity.hasCapability(CapabilityHeartHandler.CAPABILITY, null)) {
			return entity.getCapability(CapabilityHeartHandler.CAPABILITY, null);
		}
		return null;
	}
	
	public void preTick() {
		EntityPlayer player = getPlayer();
		if (player == null) return;
		IHeartHandler ihh = getHealthHandler(player);
		if (ihh == null) return;
		float hp = 0;
		int destroyed = 0;
		for (int i = 0; i < ihh.getContainers(); i++) {
			HeartContainer hc = ihh.getContainer(i);
			IGlassHeart igh = hc.getOwner();
			if (igh != null) {
				int amt = (int)((igh.getLifeforce()/((float)igh.getLifeforceCapacity()))*255);
				if (hc.getFillAmountInt() != amt || hc.getGem() != igh.getGem()) {
					hc = hc.copy();
					hc.setFillAmountInt(amt);
					hc.setGem(igh.getGem());
					ihh.setContainer(i, hc);
				}
			} else if (hc.getOwnerPos() != null) {
				ihh.removeContainer(i);
				new PlayHeartEffectMessage(PlayHeartEffectMessage.EFFECT_HEART_SHATTER, hc.getGlassColor().ordinal(), i+destroyed).sendTo(player);
				if (hc.getGem() != EnumGem.NONE) {
					new PlayHeartEffectMessage(PlayHeartEffectMessage.EFFECT_GEM_SHATTER, hc.getGem().ordinal(), i+destroyed).sendTo(player);
				}
				destroyed++;
				i--;
			}
		}
		for (HeartContainer hc : ihh) {
			hp += hc.getFillAmount();
		}
		if (player.isEntityAlive()) {
			player.setHealth(hp*2);
		} else {
			player.setHealth(0);
		}
	}
	
	public void postTick() {
		EntityPlayer player = getPlayer();
		if (player == null) return;
		resync();
	}
	
	
	
	public void resync() {
		EntityPlayer player = getPlayer();
		if (player == null) return;
		IHeartHandler ihh = getHealthHandler(player);
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
	
}
