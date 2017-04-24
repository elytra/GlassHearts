package com.elytradev.glasshearts.network;

import java.util.List;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.client.ClientProxy;
import com.elytradev.glasshearts.logic.HeartContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class UpdateHeartsMessage extends Message {

	@MarshalledAs("u8")
	private int startIndex;
	@MarshalledAs(HeartContainerMarshaller.NAME+"-list")
	private List<HeartContainer> containers;
	
	public UpdateHeartsMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public UpdateHeartsMessage(int startIndex, boolean overwrite, List<HeartContainer> containers) {
		super(GlassHearts.inst.NETWORK);
		this.startIndex = startIndex;
		if (overwrite) {
			this.startIndex |= (1 << 7);
		}
		this.containers = containers;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		boolean overwrite = (startIndex & (1 << 7)) != 0;
		int startIndex = this.startIndex & ~(1 << 7);
		List<HeartContainer> li = ((ClientProxy)GlassHearts.proxy).heartRenderer.containers;
		if (overwrite) {
			li.clear();
		}
		for (int i = 0; i < containers.size(); i++) {
			if (startIndex+i >= li.size()) {
				if (containers.get(i) != null) {
					li.add(containers.get(i));
				}
			} else {
				HeartContainer old = li.get(startIndex+i);
				HeartContainer nw = containers.get(i);
				if (nw == null) {
					li.remove(startIndex+i);
					startIndex--;
				} else if (old != null &&
						old.getGlassColor() == nw.getGlassColor() &&
						old.getGem() == nw.getGem()) {
					old.setLastFillAmount(old.getLastFillAmount());
					old.setFillAmount(nw.getFillAmount());
				} else {
					li.set(startIndex+i, nw);
				}
			}
		}
	}

}
