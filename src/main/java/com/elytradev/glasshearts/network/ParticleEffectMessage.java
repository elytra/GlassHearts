package com.elytradev.glasshearts.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.client.ParticleAttune;
import com.elytradev.glasshearts.client.ParticleRedstoneSeekEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class ParticleEffectMessage extends Message {

	public static final int EFFECT_SAP = 0;
	public static final int EFFECT_ATTUNE = 1;
	public static final int EFFECT_AMETHYST = 2;
	public static final int EFFECT_TOPAZ = 3;
	
	@MarshalledAs("i32")
	public int posXFixed;
	@MarshalledAs("i32")
	public int posYFixed;
	@MarshalledAs("i32")
	public int posZFixed;
	@MarshalledAs("i32")
	public int targetid;
	
	@MarshalledAs("u8")
	public int effect;
	
	public ParticleEffectMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public ParticleEffectMessage(double x, double y, double z, Entity target, int effect) {
		super(GlassHearts.inst.NETWORK);
		this.posXFixed = (int)(x*32D);
		this.posYFixed = (int)(y*32D);
		this.posZFixed = (int)(z*32D);
		this.targetid = target.getEntityId();
		this.effect = effect;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Entity target = sender.world.getEntityByID(targetid);
		
		if (target == null) return;
		
		double posX = posXFixed/32D;
		double posY = posYFixed/32D;
		double posZ = posZFixed/32D;
		
		float r = 0.1f;
		for (int i = 0; i < (effect == 0 ? 128 : 256); i++) {
			float x = (float)(target.world.rand.nextGaussian());
			float y = (float)(target.world.rand.nextGaussian());
			float z = (float)(target.world.rand.nextGaussian());

			if (effect == EFFECT_SAP) {
				ParticleRedstoneSeekEntity p = new ParticleRedstoneSeekEntity(target, target.world, posX+(r*x), posY+(r*y), posZ+(r*z), 1, 0.8f, 0, 0);
				p.setVelocity(x/4, y/4, z/4);
				Minecraft.getMinecraft().effectRenderer.addEffect(p);
			} else if (effect == EFFECT_ATTUNE) {
				ParticleAttune p = new ParticleAttune(target, target.world, posX+(r*x), posY+(r*y), posZ+(r*z), 1, 0.8f, 0, 0);
				p.setVelocity(x/4, y/4, z/4);
				Minecraft.getMinecraft().effectRenderer.addEffect(p);
			} else if (effect == EFFECT_AMETHYST) {
				Particle p = new ParticleSpell.MobFactory().createParticle(0, target.world, posX+x, posY+y, posZ+z, 0.80, 0.36, 0.67);
				p.multiplyVelocity(4);
				Minecraft.getMinecraft().effectRenderer.addEffect(p);
			} else if (effect == EFFECT_TOPAZ) {
				Particle p = new ParticleSpell.MobFactory().createParticle(0, target.world, posX+x, posY+y, posZ+z, 0.15, 0.32, 0.65);
				p.multiplyVelocity(4);
				Minecraft.getMinecraft().effectRenderer.addEffect(p);
			}
		}
	}

}
