package com.elytradev.glasshearts.network;

import com.elytradev.concrete.network.Marshaller;
import com.elytradev.glasshearts.EnumGem;
import com.elytradev.glasshearts.EnumGlassColor;
import com.elytradev.glasshearts.HeartContainer;

import io.netty.buffer.ByteBuf;

public class HeartContainerMarshaller implements Marshaller<HeartContainer> {

	public static final String NAME = "com.elytradev.glasshearts.network.HeartContainerMarshaller";
	public static final HeartContainerMarshaller INSTANCE = new HeartContainerMarshaller();
	
	@Override
	public HeartContainer unmarshal(ByteBuf in) {
		int kind = in.readUnsignedByte();
		EnumGlassColor color;
		if (kind == 0) {
			// natural
			color = null;
		} else {
			color = EnumGlassColor.values()[kind-1];
		}
		EnumGem gem = EnumGem.values()[in.readUnsignedByte()];
		float fill = in.readUnsignedByte()/255f;
		return new HeartContainer(color, gem, fill);
	}
	@Override
	public void marshal(ByteBuf out, HeartContainer t) {
		// TODO Auto-generated method stub
		
	}
	
}
