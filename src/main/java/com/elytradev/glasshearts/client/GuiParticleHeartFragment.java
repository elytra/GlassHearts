package com.elytradev.glasshearts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiParticleHeartFragment extends GuiParticle {

	private float u;
	private float v;
	
	public GuiParticleHeartFragment(double x, double y, float u, float v) {
		super(x, y);
		this.u = u;
		this.v = v;
		
		motionX = rand.nextGaussian()*0.6;
		motionY = -1;
		
		maxAge = Integer.MAX_VALUE;
	}
	
	@Override
	public void render(float partialTicks) {
		float interpPosX = (float)(prevPosX + (posX - prevPosX) * partialTicks);
		float interpPosY = (float)(prevPosY + (posY - prevPosY) * partialTicks);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(HeartRenderer.TEX);
		GlStateManager.color(red, green, blue);
		HeartRenderer.drawModalRectWithCustomSizedTexture(interpPosX, interpPosY, u, v, 1, 1, HeartRenderer.TEXTURE_WIDTH, HeartRenderer.TEXTURE_HEIGHT);
	}

}
