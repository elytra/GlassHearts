package com.elytradev.glasshearts.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.client.ParticleRedstoneSeekEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class SapEffectMessage extends Message {

	@MarshalledAs("i32")
	public int sappedId;
	@MarshalledAs("i32")
	public int sapperId;
	
	public SapEffectMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SapEffectMessage(Entity sapped, Entity sapper) {
		super(GlassHearts.inst.NETWORK);
		this.sappedId = sapped.getEntityId();
		this.sapperId = sapper.getEntityId();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Entity sapped = sender.world.getEntityByID(sappedId);
		Entity sapper = sender.world.getEntityByID(sapperId);
		
		if (sapped == null || sapper == null) return;
		
		float r = 0.1f;
		for (int i = 0; i < 128; i++) {
			float x = (float)(sapped.world.rand.nextGaussian());
			float y = (float)(sapped.world.rand.nextGaussian());
			float z = (float)(sapped.world.rand.nextGaussian());

			ParticleRedstoneSeekEntity p = new ParticleRedstoneSeekEntity(sapper, sapped.world, sapped.posX+(r*x), sapped.posY+(r*y), sapped.posZ+(r*z), 1, 0.8f, 0, 0);
			p.setVelocity(x/4, y/4, z/4);
			Minecraft.getMinecraft().effectRenderer.addEffect(p);
		}
	}

}
