package com.elytradev.glasshearts.client;


import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;

public class HeartRenderer extends Gui {

	private static final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");
	private static final ResourceLocation HEART_OVERLAYS = new ResourceLocation("glasshearts", "textures/gui/heart_overlays.png");
	private static final ResourceLocation GEM_OVERLAYS = new ResourceLocation("glasshearts", "textures/gui/gem_overlays.png");
	
	private int healthUpdateCounter, updateCounter;
	private float playerHealth, lastPlayerHealth;
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
		mc.mcProfiler.startSection("glassHearts");
		
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();
		
		GlStateManager.enableBlend();
		GlStateManager.pushMatrix();

		EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
		float health = player.getHealth();
		boolean highlight = healthUpdateCounter > updateCounter
				&& (healthUpdateCounter - updateCounter) / 3 % 2 == 1;

		if (health < playerHealth && player.hurtResistantTime > 0) {
			lastSystemTime = Minecraft.getSystemTime();
			healthUpdateCounter = updateCounter + 20;
		} else if (health > playerHealth && player.hurtResistantTime > 0) {
			lastSystemTime = Minecraft.getSystemTime();
			healthUpdateCounter = updateCounter + 10;
		}

		if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L) {
			playerHealth = health;
			lastPlayerHealth = health;
			lastSystemTime = Minecraft.getSystemTime();
		}
		
		playerHealth = health;
		float healthLast = lastPlayerHealth;

		IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		float healthMax = (float) attrMaxHealth.getAttributeValue();
		float absorb = player.getAbsorptionAmount();
		PotionEffect absorption = mc.player.getActivePotionEffect(MobEffects.ABSORPTION);
		int maxAbsorbHearts = absorption == null ? 0 : (absorption.getAmplifier()+1)*2;

		int healthRows = MathHelper.ceil((healthMax + (maxAbsorbHearts*2)) / 2f / (useMoreSpace ? 22f : 10f));
		int rowHeight = Math.max(10 - (healthRows - 2), 3);

		int maxHearts = MathHelper.ceil(healthMax/2f);
		
		rand.setSeed((updateCounter) * 312871L);

		int left = width / 2 - 91;
		int top = height - GuiIngameForge.left_height;
		GuiIngameForge.left_height += (healthRows * rowHeight);
		if (rowHeight != 10) {
			GuiIngameForge.left_height += 10 - rowHeight;
		}

		float regen = -1;
		if (player.isPotionActive(MobEffects.REGENERATION)) {
			regen = (updateCounter+partialTicks) % (maxHearts+15);
		}

		final int TOP = 9 * (mc.world.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
		final int BACKGROUND = (highlight ? 25 : 16);
		int MARGIN = 16;
		if (player.isPotionActive(MobEffects.POISON)) {
			MARGIN += 36;
		} else if (player.isPotionActive(MobEffects.WITHER)) {
			MARGIN += 72;
		}
		
		
		float hearts = health/2f;
		float lastHearts = healthLast/2f;
		
		float absorbHearts = absorb/2f;
		
		// absorption
		drawHealthBar(false, rowHeight, left, top, TOP, BACKGROUND, 160, absorbHearts, absorbHearts, maxAbsorbHearts, -1, false, maxHearts);
		// normal health
		drawHealthBar(highlight, rowHeight, left, top, TOP, BACKGROUND, MARGIN+36, hearts, lastHearts, maxHearts, regen, true, 0);
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		mc.mcProfiler.endSection();
	}

	public void drawHealthBar(boolean highlight, int rowHeight, int left, int top, final int v, final int backgroundU,
			int foregroundU, float hearts, float lastHearts, int maxHearts, float regen, boolean doShake, int start) {
		float fifth = (maxHearts/5f);
		for (int j = (maxHearts-1)+start; j >= start; j--) {
			int i = (j - start);
			int x = left+((j%(useMoreSpace?22:10))*8);
			float y = top-(rowHeight*(j/(useMoreSpace?22:10)));
			if (doShake && hearts <= fifth) {
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
			/*if (i+1 <= fifth) {
				GlStateManager.color(1f, 0.5f, 0f);
				drawTexturedModalRect(x, y, backgroundU+18, v, 9, 9);
				GlStateManager.color(1, 1, 1);
			} else if (MathHelper.ceiling_float_int(fifth) == (i+1)) {
				float ofs = (fifth%1)*9;
				GlStateManager.color(1f, 0.5f, 0f);
				drawTexturedModalRect(x, y, backgroundU+18, v, ofs, 9);
				GlStateManager.color(1, 1, 1);
				drawTexturedModalRect(x+ofs, y, backgroundU+ofs, v, 9-ofs, 9);
			} else {*/
				drawTexturedModalRect(x, y, backgroundU, v, 9, 9);
			//}
			if (hearts >= (i+1)) {
				drawTexturedModalRect(x, y, foregroundU, v, 9, 9);
			} else if (MathHelper.ceil(hearts) == (i+1)) {
				drawTexturedModalRect(x, y, foregroundU, v, (hearts%1)*9, 9);
			}
			if (highlight && (i+1) >= MathHelper.ceil(hearts) && hearts < lastHearts) {
				float w = 0;
				float ofs = 0;
				if (lastHearts >= (i+1) && (i+1) > hearts) {
					w = 9;
				} else if (MathHelper.ceil(lastHearts) == (i+1)) {
					w = (lastHearts%1)*9;
				}
				if (MathHelper.ceil(hearts) == (i+1)) {
					ofs = (hearts%1)*9;
					w -= ofs;
				}
				drawTexturedModalRect(x+ofs, y, foregroundU+18+ofs, v, w, 9);
			}
		}
	}
	
	public void drawTexturedModalRect(float x, float y, float textureX, float textureY, float width, float height) {
		float xScale = 1/256f;
		float yScale = 1/256f;
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x + 0, y + height, zLevel).tex((textureX + 0) * xScale, (textureY + height) * yScale).endVertex();
		worldrenderer.pos(x + width, y + height, zLevel).tex((textureX + width) * xScale, (textureY + height) * yScale).endVertex();
		worldrenderer.pos(x + width, y + 0, zLevel).tex((textureX + width) * xScale, (textureY + 0) * yScale).endVertex();
		worldrenderer.pos(x + 0, y + 0, zLevel).tex((textureX + 0) * xScale, (textureY + 0) * yScale).endVertex();
		tessellator.draw();
	}

}
