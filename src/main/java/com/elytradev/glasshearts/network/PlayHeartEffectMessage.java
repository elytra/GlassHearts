package com.elytradev.glasshearts.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.client.ClientProxy;
import com.elytradev.glasshearts.client.GuiParticleHeartFragment;
import com.elytradev.glasshearts.client.PendingEffect;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGlassColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class PlayHeartEffectMessage extends Message {

	@MarshalledAs("u8")
	private int effect;
	@MarshalledAs("u8")
	private int index;
	
	public PlayHeartEffectMessage(NetworkContext ctx) {
		super(ctx);
	}
	public PlayHeartEffectMessage(int effect, int index) {
		super(GlassHearts.inst.NETWORK);
		this.effect = effect;
		this.index = index;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Minecraft mc = Minecraft.getMinecraft();
		ClientProxy cp = ((ClientProxy)GlassHearts.proxy);
		if (effect == 0) {
			// colorless heart shatter
			cp.heartRenderer.pendingEffects.add(new PendingEffect(index, (x, y) -> {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_GLASS_BREAK, 1f));
				GuiParticleHeartFragment.spawn(cp.guiParticles, x, y, 27, 36, 9, 9, 3, 3, 1, 1, 1);
			}));
		} else if (effect < EnumGlassColor.values().length) {
			// stained heart shatter
			EnumGlassColor egc = EnumGlassColor.values()[effect];
			float[] fleece = EntitySheep.getDyeRgb(egc.dye);
			cp.heartRenderer.pendingEffects.add(new PendingEffect(index, (x, y) -> {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_GLASS_BREAK, 1f));
				GuiParticleHeartFragment.spawn(cp.guiParticles, x, y, 45, 36, 9, 9, 3, 3, fleece[0], fleece[1], fleece[2]);
			}));
		} else if (effect < (EnumGlassColor.values().length+EnumGem.values().length)-1) {
			// gem shatter
			EnumGem gem = EnumGem.values()[(effect-EnumGlassColor.values().length)+1];
			cp.heartRenderer.pendingEffects.add(new PendingEffect(index, (x, y) -> {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_GLASS_BREAK, 2f));
				GuiParticleHeartFragment.spawn(cp.guiParticles, x, y, 27+(gem.ordinal()*9), 54, 9, 9, 1, 1, 1, 1, 1);
			}));
		}
	}

}
