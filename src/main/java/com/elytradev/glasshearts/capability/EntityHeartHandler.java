package com.elytradev.glasshearts.capability;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.logic.HeartContainer;
import com.elytradev.glasshearts.logic.IGlassHeart;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

public class EntityHeartHandler extends BasicHeartHandler {

	protected EntityLivingBase elb;
	protected boolean initialized = false;
	
	public EntityHeartHandler(EntityLivingBase elb) {
		this.elb = elb;
	}
	
	@Override
	public float damage(float amount, DamageSource src) {
		float origAmount = amount;
		for (int i = getContainers()-1; i >= 0; i--) {
			if (amount <= 0) break;
			HeartContainer orig = getContainer(i);
			HeartContainer hc = orig.copy();
			float consumed = hc.damage(amount, src);
			if (consumed > 0) {
				IGlassHeart owner = hc.getOwner();
				if (owner != null && elb instanceof EntityPlayer) {
					float diff = orig.getFillAmount()-hc.getFillAmount();
					int mb = (int)(diff*owner.getLifeforceCapacity());
					((EntityPlayer)elb).addStat(GlassHearts.inst.LIFEFORCE_CONSUMED, mb);
				}
				amount -= consumed;
				setContainer(i, hc);
			}
		}
		return origAmount-amount;
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
