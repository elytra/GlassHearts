package com.elytradev.glasshearts.gem;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.elytradev.glasshearts.network.ParticleEffectMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class GemAmethyst extends Gem {
	public GemAmethyst() {
		super(0x5A399B, "gemAmethyst");
		setDefaultTexture(2);
	}
	
	@Override
	public EnumGemState getState(IGlassHeart igh) {
		return igh.hasBeenFull() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	@Override
	public void onEmpty(IGlassHeart igh) {
		for (EntityPlayer ep : GlassHearts.getAllOnlineAttunedPlayers(igh)) {
			new ParticleEffectMessage(ep.posX, ep.posY+(ep.height/2), ep.posZ, ep, ParticleEffectMessage.EFFECT_AMETHYST).sendToAllWatchingAndSelf(ep);
			// 10 seconds of Regen II
			ep.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 1));
		}
	}
}
