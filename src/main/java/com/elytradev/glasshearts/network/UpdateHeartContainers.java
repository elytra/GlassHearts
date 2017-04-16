package com.elytradev.glasshearts.network;

import java.util.List;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.glasshearts.HeartContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.CLIENT)
public class UpdateHeartContainers extends Message {

	@MarshalledAs("u8")
	private int startIndex;
	@MarshalledAs(HeartContainerMarshaller.NAME+"-list")
	private List<HeartContainer> containers;
	
	public UpdateHeartContainers(NetworkContext ctx) {
		super(ctx);
	}

	@Override
	protected void handle(EntityPlayer sender) {
		// TODO Auto-generated method stub

	}

}
