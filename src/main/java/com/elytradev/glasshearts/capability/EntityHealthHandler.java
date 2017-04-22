package com.elytradev.glasshearts.capability;

import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.logic.HeartContainer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

public class EntityHealthHandler extends BasicHealthHandler {

	protected EntityLivingBase elb;
	protected boolean initialized = false;
	
	public EntityHealthHandler(EntityLivingBase elb) {
		this.elb = elb;
	}
	
	@Override
	protected NonNullList<HeartContainer> getList() {
		if (!initialized) {
			int max = MathHelper.ceil(elb.getMaxHealth()/2);
			float hp = elb.getHealth()/2;
			for (int i = 0; i < max; i++) {
				super.getList().add(HeartContainer.createNatural(EnumGem.NONE, Math.min(Math.max(hp, 0), 1)));
				hp--;
			}
			initialized = true;
		}
		return super.getList();
	}
	
}
