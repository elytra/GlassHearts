package com.elytradev.glasshearts.capability;

import java.util.Iterator;

import com.elytradev.glasshearts.logic.HeartContainer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;

public class BasicHeartHandler implements IHeartHandler {
	
	protected final NonNullList<HeartContainer> list = NonNullList.create();

	@Override
	public float damage(float amount, DamageSource src) {
		float origAmount = amount;
		for (int i = getContainers()-1; i >= 0; i--) {
			if (amount <= 0) break;
			HeartContainer hc = getContainer(i).copy();
			float consumed = hc.damage(amount, src);
			if (consumed > 0) {
				amount -= consumed;
				setContainer(i, hc);
			}
		}
		return origAmount-amount;
	}
	
	@Override
	public float heal(float amount) {
		float origAmount = amount;
		for (int i = 0; i < getContainers(); i++) {
			if (amount <= 0) break;
			HeartContainer hc = getContainer(i).copy();
			float consumed = hc.heal(amount);
			if (consumed > 0) {
				setContainer(i, hc);
				amount -= consumed;
			}
		}
		return origAmount-amount;
	}
	
	protected NonNullList<HeartContainer> getList() {
		return list;
	}
	
	
	@Override
	public int getContainers() {
		return getList().size();
	}

	@Override
	public HeartContainer getContainer(int index) {
		return getList().get(index);
	}
	
	@Override
	public HeartContainer copyContainer(int index) {
		return getContainer(index).copy();
	}

	@Override
	public HeartContainer popContainer(int index) {
		return getList().remove(index);
	}

	@Override
	public void setContainer(int index, HeartContainer container) {
		getList().set(index, container);
	}

	@Override
	public void removeContainer(int index) {
		getList().remove(index);
	}

	@Override
	public void addContainer(HeartContainer container) {
		getList().add(container);
	}

	@Override
	public void addContainer(int index, HeartContainer container) {
		getList().add(index, container);
	}
	
	@Override
	public void clear() {
		getList().clear();
	}
	
	@Override
	public Iterator<HeartContainer> iterator() {
		return getList().iterator();
	}
	
	@Override
	public float totalHealth() {
		float total = 0;
		for (HeartContainer hc : this) {
			total += hc.getFillAmount();
		}
		return total;
	}

}
