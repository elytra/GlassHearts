package com.elytradev.glasshearts.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiParticleHeartFragment extends GuiParticle {

	private float u;
	private float v;
	
	private int w;
	private int h;
	
	public GuiParticleHeartFragment(double x, double y, float u, float v, int w, int h) {
		super(x, y);
		this.u = u;
		this.v = v;
		
		this.w = w;
		this.h = h;
		
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
		HeartRenderer.drawModalRectWithCustomSizedTexture(interpPosX, interpPosY, u, v, w, h, HeartRenderer.TEXTURE_WIDTH, HeartRenderer.TEXTURE_HEIGHT);
	}

	public static void spawn(List<GuiParticle> guiParticles, int x, int y, float u, float v, int width, int height, int particleWidth, int particleHeight, float r, float g, float b) {
		int divW = (width/particleWidth);
		int divH = (height/particleHeight);
		for (int i = 0; i < divW*divH; i++) {
			GuiParticleHeartFragment p = new GuiParticleHeartFragment(x+((i%divW)*particleWidth), y+((i/divW)*particleHeight), u+((i%divW)*particleWidth), v+((i/divW)*particleHeight), particleWidth, particleHeight);
			p.red = r;
			p.green = g;
			p.blue = b;
			guiParticles.add(p);
		}
	}

}
