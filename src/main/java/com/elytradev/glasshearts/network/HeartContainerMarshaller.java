package com.elytradev.glasshearts.network;

import io.github.elytra.concrete.Marshaller;
import io.github.elytra.concrete.exception.BadMessageException;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.logic.HeartContainer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.buffer.ByteBuf;

public class HeartContainerMarshaller implements Marshaller<HeartContainer> {

	public static final String NAME = "com.elytradev.glasshearts.network.HeartContainerMarshaller";
	public static final HeartContainerMarshaller INSTANCE = new HeartContainerMarshaller();
	
	private static final BiMap<Integer, HeartContainer> states;
	
	static {
		states = HashBiMap.create(256);
		int id = 0;
		
		states.put(id++, null);
		for (EnumGem gem : EnumGem.values()) {
			states.put(id++, HeartContainer.createNatural(gem, 0));
			for (EnumGlassColor color : EnumGlassColor.values()) {
				states.put(id++, HeartContainer.createGlass(color, gem, 0));
			}
		}
		if (states.size() > 256) {
			throw new AssertionError("Too many states! ("+states.size()+" > 256)");
		}
	}
	
	@Override
	public HeartContainer unmarshal(ByteBuf in) {
		int id = in.readUnsignedByte();
		if (id == 0) return null;
		HeartContainer template = states.get(id);
		if (template == null) throw new BadMessageException("Invalid heart state ID "+id);
		HeartContainer hc = template.copy();
		float fill = in.readUnsignedByte()/255f;
		hc.setFillAmount(fill);
		return hc;
	}
	@Override
	public void marshal(ByteBuf out, HeartContainer t) {
		if (t == null) {
			out.writeByte(0);
			return;
		}
		HeartContainer prototype = t.copy();
		prototype.setFillAmount(0f);
		prototype.setOwner(null);
		Integer idx = states.inverse().get(prototype);
		if (idx == null) {
			throw new IllegalArgumentException("Cannot find a state ID for "+prototype);
		}
		out.writeByte(idx);
		out.writeByte((int)(t.getFillAmount()*255));
	}
	
}
