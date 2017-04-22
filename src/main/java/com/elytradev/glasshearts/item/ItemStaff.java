package com.elytradev.glasshearts.item;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.network.ParticleEffectMessage;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

public class ItemStaff extends Item {

	private final DamageSource TRANSFER = new DamageSource("glasshearts.transfer").setDamageBypassesArmor().setDamageIsAbsolute();
	
	public ItemStaff() {
		setMaxStackSize(1);
		setMaxDamage(864);
		setUnlocalizedName("glasshearts.staff");
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getItemEnchantability() {
		return 22;
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if (target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) return false;
		target.attackEntityFrom(DamageSource.causeMobDamage(attacker), 4f);
		attacker.heal(1f);
		stack.damageItem(8, attacker);
		target.playSound(GlassHearts.inst.SAP, 1f, 1f);
		new ParticleEffectMessage(target.posX, target.posY, target.posZ, attacker, 0).sendToAllWatching(target);
		return true;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		playerIn.hurtResistantTime = 0;
		playerIn.attackEntityFrom(TRANSFER, 4f);
		if (target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
			target.attackEntityFrom(DamageSource.causePlayerDamage(playerIn), 8f);
		} else {
			target.heal(3f);
		}
		stack.damageItem(4, playerIn);
		target.playSound(GlassHearts.inst.SAP, 1f, 1f);
		playerIn.addStat(GlassHearts.inst.HEALTH_TRANSFERRED, 2);
		new ParticleEffectMessage(playerIn.posX, playerIn.posY, playerIn.posZ, target, 0).sendToAllWatching(target);
		return true;
	}
	
}
