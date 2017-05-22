package com.elytradev.glasshearts.gem;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.elytradev.glasshearts.network.ParticleEffectMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class GemTopaz extends Gem {
	public GemTopaz() {
		super(0xB27639, "gemTopaz");
		setDefaultTexture(5);
		
	}
	@Override
	public EnumGemState getState(IGlassHeart igh) {
		return igh.hasBeenFull() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	@Override
	public void onEmpty(IGlassHeart igh) {
		for (EntityPlayer ep : GlassHearts.getAllOnlineAttunedPlayers(igh)) {
			ParticleEffectMessage msg = new ParticleEffectMessage(ep.posX, ep.posY+(ep.height/2), ep.posZ, ep, ParticleEffectMessage.EFFECT_TOPAZ);
			msg.sendToAllWatching(ep);
			msg.sendTo(ep);
			// 30 seconds of Absorption
			ep.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 600, 0));
		}
	}
}
