package com.elytradev.glasshearts.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.glasshearts.GlassHearts;

import net.minecraft.entity.player.EntityPlayer;
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

	}

}
