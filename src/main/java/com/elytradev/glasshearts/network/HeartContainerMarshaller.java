package com.elytradev.glasshearts.network;

import java.util.Map;

import com.elytradev.concrete.network.Marshaller;
import com.elytradev.glasshearts.EnumGem;
import com.elytradev.glasshearts.EnumGlassColor;
import com.elytradev.glasshearts.HeartContainer;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;

public class HeartContainerMarshaller implements Marshaller<HeartContainer> {

	public static final String NAME = "com.elytradev.glasshearts.network.HeartContainerMarshaller";
	public static final HeartContainerMarshaller INSTANCE = new HeartContainerMarshaller();
	
	private static final ImmutableBiMap<Integer, HeartContainer> states;
	
	static {
		Map<Integer, HeartContainer> builder = Maps.newHashMapWithExpectedSize(256);
		int id = 0;
		for (int i = 0; i < 10; i++) {
			builder.put(id++, new HeartContainer(null, EnumGem.NONE, i, 0f));
		}
		for (EnumGlassColor color : EnumGlassColor.values()) {
			for (EnumGem gem : EnumGem.values()) {
				builder.put(id++, new HeartContainer(color, gem, 0, 0f));
			}
		}
		if (builder.size() > 256) {
			throw new AssertionError("Too many states! ("+builder.size()+" > 256)");
		}
		states = ImmutableBiMap.copyOf(builder);
	}
	
	@Override
	public HeartContainer unmarshal(ByteBuf in) {
		HeartContainer hc = states.get((int)in.readUnsignedByte()).copy();
		float fill = in.readUnsignedByte()/255f;
		hc.setFillAmount(fill);
		return hc;
	}
	@Override
	public void marshal(ByteBuf out, HeartContainer t) {
		HeartContainer prototype = t.copy();
		if (prototype.getGlassColor() != null) {
			prototype.setDecay(0);
		}
		prototype.setFillAmount(0f);
		Integer idx = states.inverse().get(prototype);
		if (idx == null) {
			throw new AssertionError("Cannot find a state for "+t);
		}
		out.writeByte(idx);
		out.writeByte((int)(t.getFillAmount()*255));
	}
	
}
