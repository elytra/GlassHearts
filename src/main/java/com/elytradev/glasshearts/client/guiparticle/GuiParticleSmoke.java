package com.elytradev.glasshearts.client.guiparticle;

import com.elytradev.glasshearts.client.HeartRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiParticleSmoke extends GuiParticle {

	private static final ResourceLocation PARTICLES = new ResourceLocation("textures/particle/particles.png");
	
	public GuiParticleSmoke(double x, double y) {
		super(x, y);
		
		gravity = 0;
		maxAge = 20+rand.nextInt(20);
	}
	
	@Override
	public void render(float partialTicks) {
		float interpPosX = (float)(prevPosX + (posX - prevPosX) * partialTicks);
		float interpPosY = (float)(prevPosY + (posY - prevPosY) * partialTicks);
		
		int u = 8 - ((int)((age/(float)maxAge)*8));
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(PARTICLES);
		GlStateManager.color(red, green, blue);
		HeartRenderer.drawModalRectWithCustomSizedTexture(interpPosX, interpPosY, u*8, 0, 8, 8, 128, 128);
	}
	
	@Override
	public void update() {
		super.update();
		motionY -= 0.05;
	}

}
