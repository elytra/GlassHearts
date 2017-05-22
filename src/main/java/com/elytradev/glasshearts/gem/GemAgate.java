package com.elytradev.glasshearts.gem;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.elytradev.glasshearts.network.ParticleEffectMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class GemAgate extends Gem {

	public GemAgate() {
		super(0x805632, "gemAgate");
		setDefaultTexture(10);
	}
	
	@Override
	public EnumGemState getState(IGlassHeart igh) {
		return igh.hasBeenFull() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	@Override
	public void onEmpty(IGlassHeart igh) {
		for (EntityPlayer ep : GlassHearts.getAllOnlineAttunedPlayers(igh)) {
			ParticleEffectMessage msg = new ParticleEffectMessage(ep.posX, ep.posY+(ep.height/2), ep.posZ, ep, ParticleEffectMessage.EFFECT_AGATE);
			msg.sendToAllWatching(ep);
			msg.sendTo(ep);
			// 60 seconds of Strength II
			ep.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 1200, 1));
		}
	}
	
}
