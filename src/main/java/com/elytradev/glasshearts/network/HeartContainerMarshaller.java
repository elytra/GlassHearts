package com.elytradev.glasshearts.network;

import com.elytradev.concrete.network.Marshaller;
import com.elytradev.concrete.network.exception.BadMessageException;
import com.elytradev.glasshearts.enums.EnumGemOre;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.gem.Gem;
import com.elytradev.glasshearts.logic.HeartContainer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.buffer.ByteBuf;

public class HeartContainerMarshaller implements Marshaller<HeartContainer> {

	public static final String NAME = "com.elytradev.glasshearts.network.HeartContainerMarshaller";
	public static final HeartContainerMarshaller INSTANCE = new HeartContainerMarshaller();
	
	@Override
	public HeartContainer unmarshal(ByteBuf in) {
		int id = in.readUnsignedByte();
		if (id == 0) return null;
		Gem gem = Gem.getGemById(id-1);
		int colorId = in.readUnsignedByte();
		EnumGlassColor color = colorId == 0 ? null : EnumGlassColor.values()[colorId-1];
		float fill = in.readUnsignedByte()/255f;
		HeartContainer hc = new HeartContainer(color, gem, fill, null);
		return hc;
	}
	@Override
	public void marshal(ByteBuf out, HeartContainer t) {
		if (t == null) {
			out.writeByte(0);
			return;
		}
		out.writeByte(Gem.getIdForGem(t.getGem())+1);
		out.writeByte(t.getGlassColor() == null ? 0 : t.getGlassColor().ordinal()+1);
		out.writeByte(t.getFillAmountInt());
	}
	
}
