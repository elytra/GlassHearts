package com.elytradev.glasshearts.client;


import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.elytradev.glasshearts.client.guiparticle.PendingEffect;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.gem.Gem;
import com.elytradev.glasshearts.logic.HeartContainer;
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
	public static final int TEXTURE_WIDTH = 256;
	public static final int TEXTURE_HEIGHT = 256;
	
	public List<PendingEffect> pendingEffects = Lists.newArrayList();
	
	private int healthUpdateCounter, updateCounter;
	private long lastSystemTime;
	private Random rand = new Random(0);
	
	private boolean useMoreSpace;
	
	public float decay = 0;
	
	public void setUseMoreSpace(boolean useMoreSpace) {
		this.useMoreSpace = useMoreSpace;
	}
	
	public boolean useMoreSpace() {
		return useMoreSpace;
	}
	
	
	public void tick() {
		if (!Minecraft.getMinecraft().isGamePaused()) {
			updateCounter++;
		}
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
		boolean loss = false;
		for (HeartContainer hc : containers) {
			if (hc == null) continue;
			if (hc.getLastFillAmount() != hc.getFillAmount()) {
				anyNotEqual = true;
				if (hc.getLastFillAmount() > hc.getFillAmount()) {
					loss = true;
				}
				break;
			}
		}
		
		if (anyNotEqual && player.hurtResistantTime > 0 && (healthUpdateCounter-updateCounter) < 0) {
			lastSystemTime = Minecraft.getSystemTime();
			healthUpdateCounter = updateCounter + (loss ? 20 : 10);
		}

		if (Minecraft.getSystemTime() - lastSystemTime > 1000L) {
			lastSystemTime = Minecraft.getSystemTime();
			for (HeartContainer hc : containers) {
				if (hc == null) continue;
				hc.setLastFillAmount(hc.getFillAmount());
			}
		}
		
		float absorb = player.getAbsorptionAmount()/2;
		int absorbTotal = MathHelper.ceil(absorb);
		
		int totalHearts = containers.size()+absorbTotal;
		
		int healthRows = MathHelper.ceil(totalHearts/10f);
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
			regen = (updateCounter+partialTicks) % (totalHearts+15);
		}

		for (PendingEffect pe : pendingEffects) {
			int index = pe.getIndex();
			
			int x = left;
			int y = top;
			
			int diff = MathHelper.ceil((index+1)/10f) - MathHelper.ceil(Math.min(10, totalHearts)/10f);
			
			x += (index%10)*8;
			y -= (diff*rowHeight);
			pe.getEffect().spawn(x, y);
		}
		pendingEffects.clear();
		
		// we can still rely on the player's health value as a total
		boolean shake = player.getHealth() <= player.getMaxHealth()/5f;
		
		int defaultX = left+(((totalHearts+9)%10)*8);
		int defaultY = top-(healthRows-1)*rowHeight;
		
		int x = defaultX;
		int y = defaultY;
		
		for (int i = totalHearts-1; i >= 0; i--) {
			HeartContainer hc = i >= containers.size() ? null : containers.get(i);
			
			float fill;
			int uOffset;
			float nextAlpha;
			
			if (hc == null) {
				fill = (i == totalHearts-1) ? ((int)absorb == absorb) ? 1 : absorb%1 : 1;
				uOffset = -1;
				nextAlpha = 0;
			} else {
				fill = hc.getFillAmount();
				if (hc.getGlassColor() == null) {
					uOffset = (int)decay;
					nextAlpha = decay%1;
				} else {
					uOffset = 0;
					nextAlpha = 0;
				}
			}
			
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
			if (hc == null || hc.getGlassColor() == null) {
				bgFgIdx = 0;
			} else if (hc.getGlassColor() == EnumGlassColor.NONE) {
				bgFgIdx = 1;
			} else {
				bgFgIdx = 2;
			}
			// background
			drawModalRectWithCustomSizedTexture(x, y, 9+(bgFgIdx*18), 27, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			if (highlight) {
				// highlight overlay
				drawModalRectWithCustomSizedTexture(x, y, 18+(bgFgIdx*18), 27, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			}
			
			int fillIdx;
			if (player.isPotionActive(MobEffects.WITHER)) {
				fillIdx = 2;
			} else if (player.isPotionActive(MobEffects.POISON)) {
				fillIdx = 1;
			} else {
				fillIdx = 0;
			}
			if (hc != null && highlight) {
				// health highlight
				drawModalRectWithCustomSizedTexture(x, y, 36+(uOffset*9), 72+((fillIdx*18)+9), hc.getLastFillAmount()*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				if (nextAlpha > 0) {
					GlStateManager.color(1, 1, 1, nextAlpha);
					drawModalRectWithCustomSizedTexture(x, y, 36+((uOffset+1)*9), 72+((fillIdx*18)+9), hc.getLastFillAmount()*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
					GlStateManager.color(1, 1, 1, 1);
				}
			}
			// health
			drawModalRectWithCustomSizedTexture(x, y, 36+(uOffset*9), 72+((fillIdx*18)), fill*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			if (nextAlpha > 0) {
				GlStateManager.color(1, 1, 1, nextAlpha);
				drawModalRectWithCustomSizedTexture(x, y, 36+((uOffset+1)*9), 72+((fillIdx*18)), fill*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				GlStateManager.color(1, 1, 1, 1);
			}
			if (hc != null && hc.getGlassColor() == null) {
				// glint
				drawModalRectWithCustomSizedTexture(x, y, 18, 72+((fillIdx*18)), fill*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			}
			
			if (hc != null && hc.getGlassColor() != null && hc.getGlassColor().dye != null) {
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
			// foreground
			drawModalRectWithCustomSizedTexture(x+ofsL, y, 9+ofsL+(bgFgIdx*18), 36, 9-(ofsL+ofsR), 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			GlStateManager.color(1, 1, 1);
			if (highlight) {
				drawModalRectWithCustomSizedTexture(x+ofsL, y, 18+ofsL+(bgFgIdx*18), 36, 9-(ofsL+ofsR), 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			}
			if (mc.world.getWorldInfo().isHardcoreModeEnabled() && hc != null && hc.getGlassColor() == null) {
				// hardcore eyes
				drawModalRectWithCustomSizedTexture(x, y, 9, 72+((fillIdx*18)+(highlight?9:0)), fill*9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			}
			if (hc != null) {
				// gem
				Gem gem = hc.getGem();
				boolean bind = !gem.getTexture().equals(TEX);
				if (bind) {
					mc.getTextureManager().bindTexture(gem.getTexture());
				}
				drawModalRect(x, y, 9, 9, gem.getMinU(), gem.getMinV(), gem.getMaxU(), gem.getMaxV());
				if (bind) {
					mc.getTextureManager().bindTexture(TEX);
				}
			}
			
			y = oldY;
			
			x -= 8;
			if (x < left) {
				x = left+72;
				y += rowHeight;
			}
		}
		
		if (mc.gameSettings.showDebugInfo && !mc.isReducedDebug()) {
			String a = Float.toString(player.getHealth()/2f);
			mc.fontRendererObj.drawString(a, left-2-mc.fontRendererObj.getStringWidth(a), y-9, 0xFFFF0000);
			String b = Float.toString(absorb);
			mc.fontRendererObj.drawString(b, left-2-mc.fontRendererObj.getStringWidth(b), top-10, 0xFFFFFF00);
			GlStateManager.color(1, 1, 1);
		}
		
		GlStateManager.popMatrix();
		GlStateManager.disableBlend();
		
		mc.mcProfiler.endSection();
	}

	public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
		float xScale = 1 / textureWidth;
		float yScale = 1 / textureHeight;
		drawModalRect(x, y, width, height, u * xScale, v * yScale, (u+width) * xScale, (v+height) * yScale);
	}
	
	public static void drawModalRect(float x, float y, float width, float height, float minU, float minV, float maxU, float maxV) {
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(x,         y + height, 0).tex(minU, maxV).endVertex();
		vb.pos(x + width, y + height, 0).tex(maxU, maxV).endVertex();
		vb.pos(x + width, y         , 0).tex(maxU, minV).endVertex();
		vb.pos( x      ,  y         , 0).tex(minU, minV).endVertex();
		tess.draw();
	}

}
