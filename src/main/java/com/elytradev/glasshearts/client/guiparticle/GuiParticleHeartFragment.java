package com.elytradev.glasshearts.client.guiparticle;

import java.util.List;

import com.elytradev.glasshearts.client.HeartRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class GuiParticleHeartFragment extends GuiParticle {

	private ResourceLocation texture;
	
	private float u;
	private float v;
	
	private float w;
	private float h;
	
	private int textureWidth;
	private int textureHeight;
	
	private boolean fade;
	
	public GuiParticleHeartFragment(ResourceLocation texture, double x, double y, float u, float v, float w, float h, int textureWidth, int textureHeight, boolean fade) {
		super(x, y);
		this.texture = texture;
		
		this.u = u;
		this.v = v;
		
		this.w = w;
		this.h = h;
		
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		
		this.fade = fade;
		
		if (fade) {
			motionX = 0;
			motionY = 0;
			maxAge = 10;
			gravity = 0;
		} else {
			motionX = rand.nextGaussian()*0.6;
			motionY = -1;
			maxAge = Integer.MAX_VALUE;
		}
	}
	
	@Override
	public void render(float partialTicks) {
		float interpPosX = (float)(prevPosX + (posX - prevPosX) * partialTicks);
		float interpPosY = (float)(prevPosY + (posY - prevPosY) * partialTicks);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		float alpha = 1;
		if (fade) {
			alpha = 1-(MathHelper.sin((age/(float)maxAge)*(float)(Math.PI/2)));
		}
		GlStateManager.color(red, green, blue, alpha);
		HeartRenderer.drawModalRectWithCustomSizedTexture(interpPosX, interpPosY, u, v, w, h, textureWidth, textureHeight);
	}

	public static void spawnShatter(List<GuiParticle> guiParticles, ResourceLocation texture, int x, int y, float u, float v, float width, float height, int textureWidth, int textureHeight, float particleWidth, float particleHeight, float r, float g, float b) {
		float divW = (width/particleWidth);
		float divH = (height/particleHeight);
		for (int i = 0; i < divW*divH; i++) {
			GuiParticleHeartFragment p = new GuiParticleHeartFragment(texture, x+((i%divW)*particleWidth), y+((i/divW)*particleHeight), u+((i%divW)*particleWidth), v+((i/divW)*particleHeight), particleWidth, particleHeight, textureWidth, textureHeight, false);
			p.red = r;
			p.green = g;
			p.blue = b;
			guiParticles.add(p);
		}
	}
	
	public static void spawnFade(List<GuiParticle> guiParticles, ResourceLocation texture, int x, int y, float u, float v, float width, float height, int textureWidth, int textureHeight, float r, float g, float b) {
		GuiParticleHeartFragment p = new GuiParticleHeartFragment(texture, x, y, u, v, width, height, textureWidth, textureHeight, true);
		p.red = r;
		p.green = g;
		p.blue = b;
		guiParticles.add(p);
	}

}
