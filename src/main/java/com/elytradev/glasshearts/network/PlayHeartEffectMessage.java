package com.elytradev.glasshearts.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.client.ClientProxy;
import com.elytradev.glasshearts.client.HeartRenderer;
import com.elytradev.glasshearts.client.guiparticle.GuiParticleHeartFragment;
import com.elytradev.glasshearts.client.guiparticle.GuiParticleSmoke;
import com.elytradev.glasshearts.client.guiparticle.PendingEffect;
import com.elytradev.glasshearts.client.guiparticle.PendingEffect.EffectSpawner;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.gem.Gem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class PlayHeartEffectMessage extends Message {

	public static final int EFFECT_HEART_SHATTER = 0;
	public static final int EFFECT_GEM_SHATTER = 1;
	public static final int EFFECT_WASTED_HEALING = 2;
	public static final int EFFECT_UNATTUNE = 3;
	
	@MarshalledAs("u8")
	private int effect;
	@MarshalledAs("u8")
	private int meta;
	@MarshalledAs("u8")
	private int index;
	
	public PlayHeartEffectMessage(NetworkContext ctx) {
		super(ctx);
	}
	public PlayHeartEffectMessage(int effect, int meta, int index) {
		super(GlassHearts.inst.NETWORK);
		this.effect = effect;
		this.meta = meta;
		this.index = index;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Minecraft mc = Minecraft.getMinecraft();
		ClientProxy cp = ((ClientProxy)GlassHearts.proxy);
		if (effect == EFFECT_HEART_SHATTER) {
			if (meta == 0) {
				// colorless
				cp.heartRenderer.pendingEffects.add(new PendingEffect(index, new EffectSpawner() {
					@Override
					@SideOnly(Side.CLIENT)
					public void spawn(int x, int y) {
						mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_GLASS_BREAK, 1f));
						GuiParticleHeartFragment.spawnShatter(cp.guiParticles, GlassHearts.TEX, x, y, 27, 36, 9, 9, HeartRenderer.TEXTURE_WIDTH, HeartRenderer.TEXTURE_HEIGHT, 3, 3, 1, 1, 1);
					}
				}));
			} else {
				// stained
				EnumGlassColor egc = EnumGlassColor.values()[meta];
				float[] fleece = EntitySheep.getDyeRgb(egc.dye);
				cp.heartRenderer.pendingEffects.add(new PendingEffect(index, new EffectSpawner() {
					@Override
					@SideOnly(Side.CLIENT)
					public void spawn(int x, int y) {
						mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_GLASS_BREAK, 1f));
						GuiParticleHeartFragment.spawnShatter(cp.guiParticles, GlassHearts.TEX, x, y, 45, 36, 9, 9, HeartRenderer.TEXTURE_WIDTH, HeartRenderer.TEXTURE_HEIGHT, 3, 3, fleece[0], fleece[1], fleece[2]);
					}
				}));
			}
		} else if (effect == EFFECT_GEM_SHATTER) {
			Gem gem = Gem.getGemById(meta);
			cp.heartRenderer.pendingEffects.add(new PendingEffect(index, new EffectSpawner() {
				@Override
				@SideOnly(Side.CLIENT)
				public void spawn(int x, int y) {
					mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_GLASS_BREAK, 2f));
					GuiParticleHeartFragment.spawnShatter(cp.guiParticles, gem.getTexture(), x, y, gem.getU(), gem.getV(), gem.getWidth(), gem.getHeight(), gem.getTextureWidth(), gem.getTextureHeight(), 1, 1, 1, 1, 1);
				}
			}));
		} else if (effect == EFFECT_WASTED_HEALING) {
			cp.heartRenderer.pendingEffects.add(new PendingEffect(index, new EffectSpawner() {
				@Override
				@SideOnly(Side.CLIENT)
				public void spawn(int x, int y) {
					mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_FIRE_EXTINGUISH, 2f));
					for (int i = 0; i < meta+1; i++) {
						GuiParticleSmoke s = new GuiParticleSmoke(x+5, y);
						s.motionX = (s.motionX+0.75f);
						s.motionY = (s.rand.nextFloat()*2)-1;
						s.red = 0.8f+(s.rand.nextFloat()*0.2f);
						s.green = 0;
						s.blue = 0;
						cp.guiParticles.add(s);
					}
				}
			}));
		} else if (effect == EFFECT_UNATTUNE) {
			if (meta == 0) {
				// colorless
				cp.heartRenderer.pendingEffects.add(new PendingEffect(index, new EffectSpawner() {
					@Override
					@SideOnly(Side.CLIENT)
					public void spawn(int x, int y) {
						GuiParticleHeartFragment.spawnFade(cp.guiParticles, GlassHearts.TEX, x, y, 27, 36, 9, 9, HeartRenderer.TEXTURE_WIDTH, HeartRenderer.TEXTURE_HEIGHT, 1, 1, 1);
					}
				}));
			} else {
				// stained
				EnumGlassColor egc = EnumGlassColor.values()[meta];
				float[] fleece = EntitySheep.getDyeRgb(egc.dye);
				cp.heartRenderer.pendingEffects.add(new PendingEffect(index, new EffectSpawner() {
					@Override
					@SideOnly(Side.CLIENT)
					public void spawn(int x, int y) {
						GuiParticleHeartFragment.spawnFade(cp.guiParticles, GlassHearts.TEX, x, y, 45, 36, 9, 9, HeartRenderer.TEXTURE_WIDTH, HeartRenderer.TEXTURE_HEIGHT, fleece[0], fleece[1], fleece[2]);
					}
				}));
			}
		}
	}

}
