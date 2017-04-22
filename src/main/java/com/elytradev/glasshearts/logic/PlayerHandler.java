package com.elytradev.glasshearts.logic;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.network.UpdateHeartsMessage;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerHandler implements INBTSerializable<NBTTagCompound> {

	private final WeakReference<EntityPlayer> player;
	
	private final List<HeartContainer> lastContainers = Lists.newArrayList();
	private final List<HeartContainer> containers = Lists.newArrayList();
	
	public PlayerHandler(EntityPlayer player) {
		this.player = new WeakReference<>(player);
	}
	
	protected EntityPlayer getPlayer() {
		EntityPlayer ep = player.get();
		return ep == null || ep.isDead ? null : ep;
	}
	
	public void preTick() {
		EntityPlayer player = getPlayer();
		if (player == null) return;
		if (containers.isEmpty()) {
			// is this a safe assumption? that all hearts on an uninitialized
			// player are natural?
			int max = MathHelper.ceil(player.getMaxHealth()/2);
			float hp = player.getHealth()/2;
			for (int i = 0; i < max; i++) {
				containers.add(HeartContainer.createNatural(EnumGem.NONE, Math.min(Math.max(hp, 0), 1)));
				hp--;
			}
		}
		if (containers.size() < 10) {
			containers.add(HeartContainer.createNatural(EnumGem.NONE, 1));
		} else if (containers.size() > 10) {
			containers.remove(0);
		}
		float hp = 0;
		for (HeartContainer hc : containers) {
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
		if (lastContainers.isEmpty()) {
			lastContainers.addAll(containers);
			new UpdateHeartsMessage(0, true, containers).sendTo(player);
		} else {
			int start = -1;
			List<HeartContainer> sync = Collections.emptyList();
			int lastChanged = -1;
			for (int i = 0; i < Math.max(lastContainers.size(), containers.size()); i++) {
				HeartContainer last = (i >= lastContainers.size()) ? null : lastContainers.get(i);
				HeartContainer cur = (i >= containers.size()) ? null : containers.get(i);
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
				lastContainers.addAll(containers);
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
