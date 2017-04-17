package com.elytradev.glasshearts.client;


import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.elytradev.glasshearts.EnumGlassColor;
import com.elytradev.glasshearts.HeartContainer;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;

public class HeartRenderer extends Gui {

	public List<HeartContainer> containers = Lists.newArrayList();
	
	public static final ResourceLocation TEX = new ResourceLocation("glasshearts", "textures/gui/heart.png");
	public static final int TEXTURE_WIDTH = 108;
	public static final int TEXTURE_HEIGHT = 126;
	
	private int healthUpdateCounter, updateCounter;
	private long lastSystemTime;
	private Random rand = new Random(0);
	
	private boolean useMoreSpace;
	
	public void setUseMoreSpace(boolean useMoreSpace) {
		this.useMoreSpace = useMoreSpace;
	}
	
	public boolean useMoreSpace() {
		return useMoreSpace;
	}
	
	
	public void tick() {
		updateCounter++;
	}
	
	public void renderHealth(ScaledResolution res, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.mcProfiler.startSection("glasshearts:healthbar");
		
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();
		
		GlStateManager.enableBlend();
		GlStateManager.pushMatrix();

		EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
		boolean highlight = healthUpdateCounter > (long)updateCounter && (healthUpdateCounter - (long)updateCounter) / 3 % 2 == 1;

		boolean anyNotEqual = false;
		for (HeartContainer hc : containers) {
			if (hc.getLastFillAmount() != hc.getFillAmount()) {
				anyNotEqual = true;
				break;
			}
		}
		
		if (anyNotEqual && player.hurtResistantTime > 0) {
			lastSystemTime = Minecraft.getSystemTime();
			healthUpdateCounter = updateCounter + 20;
		}

		if (Minecraft.getSystemTime() - lastSystemTime > 1000L) {
			lastSystemTime = Minecraft.getSystemTime();
			for (HeartContainer hc : containers) {
				hc.setLastFillAmount(hc.getFillAmount());
			}
		}
		
		int healthRows = MathHelper.ceil(containers.size()/10f);
		int rowHeight = Math.max(10 - (healthRows - 2), 3);

		rand.setSeed((updateCounter) * 312871L);

		int left = width / 2 - 91;
		int top = height - GuiIngameForge.left_height;
		GuiIngameForge.left_height += (healthRows * rowHeight);
		if (rowHeight != 10) {
			GuiIngameForge.left_height += 10 - rowHeight;
		}

		float regen = -1;
		if (player.isPotionActive(MobEffects.REGENERATION)) {
			regen = (updateCounter+partialTicks) % (containers.size()+15);
		}

		// we can still rely on the player's health value as a total
		boolean shake = player.getHealth() <= player.getMaxHealth()/5f;
		
		int defaultX = left+(((containers.size()+9)%10)*8);
		int defaultY = top-(healthRows-1)*rowHeight;
		
		int x = defaultX;
		int y = defaultY;
		
		
		for (int i = containers.size()-1; i >= 0; i--) {
			HeartContainer hc = containers.get(i);
			int oldY = y;
			if (shake) {
				y += rand.nextInt(2);
			}
			if (regen != -1) {
				float r = (regen-i);
				if (r >= 0 && r <= 1) {
					y -= (2*(r%1));
				} else if (r > 1 && r < 2) {
					y -= (2*(1-(r%1)));
				}
				
			}
			
			mc.getTextureManager().bindTexture(TEX);
			
			int bgFgIdx;
			if (hc.getGlassColor() == null) {
				bgFgIdx = 0;
			} else if (hc.getGlassColor() == EnumGlassColor.NONE) {
				bgFgIdx = 1;
			} else {
				bgFgIdx = 2;
			}
			drawModalRectWithCustomSizedTexture(x, y, 9+((bgFgIdx*18)+(highlight?9:0)), 27, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			
			int fillIdx;
			if (player.isPotionActive(MobEffects.WITHER)) {
				fillIdx = 2;
			} else if (player.isPotionActive(MobEffects.POISON)) {
				fillIdx = 1;
			} else {
				fillIdx = 0;
			}
			if (highlight) {
				drawModalRectWithCustomSizedTexture(x, y, 9+(hc.getDecay()*9), 72+((fillIdx*18)+9), hc.getLastFillAmount()*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			}
			drawModalRectWithCustomSizedTexture(x, y, 9+(hc.getDecay()*9), 72+((fillIdx*18)), hc.getFillAmount()*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			
			if (hc.getGlassColor() != null && hc.getGlassColor().dye != null) {
				float[] fleece = EntitySheep.getDyeRgb(hc.getGlassColor().dye);
				GlStateManager.color(fleece[0], fleece[1], fleece[2]);
			}
			float ofsL = 0.5f;
			float ofsR = 0.5f;
			if (x - 8 < left) {
				ofsL = 0;
			} else if (x == left+72 || (y == defaultY && x == defaultX)) {
				ofsR = 0;
			}
			float overlay = 1;
			if (hc.getGlassColor() == null) {
				overlay = hc.getFillAmount();
			}
			drawModalRectWithCustomSizedTexture(x+ofsL, y, 9+ofsL+((bgFgIdx*18)+(highlight?9:0)), 36, (overlay*9)-(ofsL+ofsR), 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			GlStateManager.color(1, 1, 1);
			if (mc.world.getWorldInfo().isHardcoreModeEnabled() && hc.getGlassColor() == null) {
				drawModalRectWithCustomSizedTexture(x, y, 99, 72+((fillIdx*18)+(highlight?9:0)), hc.getFillAmount()*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			}
			drawModalRectWithCustomSizedTexture(x, y, 9+(hc.getGem().ordinal()*9), 54, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			
			y = oldY;
			
			x -= 8;
			if (x < left) {
				x = left+72;
				y += rowHeight;
			}
		}
		
		GlStateManager.popMatrix();
		GlStateManager.disableBlend();
		
		mc.mcProfiler.endSection();
	}

	public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
		float xScale = 1 / textureWidth;
		float yScale = 1 / textureHeight;
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(x,         y + height, 0).tex( u          * xScale, (v + height) * yScale).endVertex();
		vb.pos(x + width, y + height, 0).tex((u + width) * xScale, (v + height) * yScale).endVertex();
		vb.pos(x + width, y         , 0).tex((u + width) * xScale,  v           * yScale).endVertex();
		vb.pos( x      ,  y         , 0).tex( u          * xScale,  v           * yScale).endVertex();
		tess.draw();
	}

}
