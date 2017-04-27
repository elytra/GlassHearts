package com.elytradev.glasshearts.network;

import com.elytradev.glasshearts.logic.HeartContainer;

import io.github.elytra.concrete.DefaultMarshallers.ListMarshaller;

public class HeartContainerListMarshaller extends ListMarshaller<HeartContainer> {

	public static final String NAME = "com.elytradev.glasshearts.network.HeartContainerListMarshaller";
	public static final HeartContainerListMarshaller INSTANCE = new HeartContainerListMarshaller();
	
	public HeartContainerListMarshaller() {
		super(HeartContainerMarshaller.INSTANCE);
	}

}
