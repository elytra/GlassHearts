package com.elytradev.glasshearts.client;

import org.lwjgl.opengl.GL11;

import com.elytradev.glasshearts.EnumGem;
import com.elytradev.glasshearts.EnumGlassColor;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.block.BlockGlassHeart;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;

public class RenderGlassHeart extends TileEntitySpecialRenderer<TileEntityGlassHeart> {

	@Override
	public void renderTileEntityAt(TileEntityGlassHeart te, double x, double y, double z, float partialTicks, int destroyStage) {
		BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockModelRenderer bmr = brd.getBlockModelRenderer();
		
		boolean stained = te.getColor() != EnumGlassColor.NONE;
		IBlockState outerState = GlassHearts.inst.GLASS_HEART.getDefaultState().withProperty(BlockGlassHeart.INNER, false).withProperty(BlockGlassHeart.STAINED, stained);
		
		IBakedModel outer = brd.getModelForState(outerState);

		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x, y, z);
		float t = (((te.getWorld().getTotalWorldTime()+te.hashCode())%24000)+partialTicks);
		GlStateManager.translate(0.5f, 0.5f+(MathHelper.sin(t/12f)/16f), 0.5f);
		GlStateManager.rotate(t%360, 0, 1, 0);
		GlStateManager.translate(-0.5f, -0.5f, -0.5f);
		if (destroyStage >= 0) {
			bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4f, 4f, 1f);
			GlStateManager.translate(0.0625f, 0.0625f, 0.0625f);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		} else {
			GlStateManager.color(1, 1, 1, 1);
		}
		
		setLightmapDisabled(false);
		
		GlStateManager.enableRescaleNormal();
		GlStateManager.disableBlend();
		
		if (MinecraftForgeClient.getRenderPass() == 0) {
			float partial = te.getLifeforceBuffer() > 0 ? partialTicks : 0;
			if (te.getGem() == EnumGem.OPAL) {
				partial = ((te.getWorld().getTotalWorldTime()+partialTicks)%10)/10f;
			}
			float amt = ((te.getLifeforce()+partial)/GlassHearts.inst.configGlassHeartCapacity)*14f;
			float bufferAmt = (te.getLifeforceBuffer()/(float)GlassHearts.inst.configGlassHeartCapacity)*14f;
			
			renderFill(amt);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.5f, 0.5f, 0.5f);
			GlStateManager.scale(0.99f, 0.99f, 0.99f);
			GlStateManager.translate(-0.5f, -0.5f, -0.5f);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE,
					SourceFactor.SRC_ALPHA, DestFactor.ONE);
			renderFill(bufferAmt+amt);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
				GlStateManager.translate(0.5f, 0.55f, 0.5f);
				GlStateManager.pushMatrix();
					GlStateManager.translate(0, 0, -0.265f);
					GlStateManager.scale(0.45f, 0.45f, 0.45f);
					Minecraft.getMinecraft().getRenderItem().renderItem(te.getGem().getRenderingSingleton(), TransformType.FIXED);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
					GlStateManager.translate(0, 0, 0.265f);
					GlStateManager.scale(0.45f, 0.45f, 0.45f);
					Minecraft.getMinecraft().getRenderItem().renderItem(te.getGem().getRenderingSingleton(), TransformType.FIXED);
				GlStateManager.popMatrix();
			GlStateManager.popMatrix();
		}
		
		GlStateManager.depthMask(true);
		
		if (stained) {
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
					SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		} else {
			GlStateManager.enableAlpha();
			GlStateManager.disableBlend();
		}
		if (stained && MinecraftForgeClient.getRenderPass() == 1) {
			float[] color = EntitySheep.getDyeRgb(te.getColor().dye);
			bmr.renderModelBrightnessColor(outerState, outer, 1f, color[0], color[1], color[2]);
		} else if (MinecraftForgeClient.getRenderPass() == 0) {
			bmr.renderModelBrightnessColor(outerState, outer, 1f, 1f, 1f, 1f);
		}
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		
		if (destroyStage >= 0) {
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}
		
		GlStateManager.popMatrix();
		
		super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
	}

	private void renderFill(float amt) {
		float veryBottomElement = Math.min(amt, 2);
		float bottomElement = Math.max(0, Math.min(amt-2, 2));
		float midElement = Math.max(0, Math.min(amt-4, 6));
		float topElement = Math.max(0, Math.min(amt-10, 2));
		
		TextureAtlasSprite tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("glasshearts:blocks/lifeforce_still");
		
		if (veryBottomElement > 0) {
			renderCube(6, 2, 6, 4, veryBottomElement, 4, tas, bottomElement <= 0, true, true);
		}
		if (bottomElement > 0) {
			renderCube(4, 4, 6, 8, bottomElement, 4, tas, midElement <= 0, false, true);
		}
		if (midElement > 0) {
			renderCube(2, 6, 6, 12, midElement, 4, tas, topElement <= 0, false, true);
		}
		if (topElement > 0) {
			renderCube(2, 12, 6, 5, topElement, 4, tas, true, false, true);
			renderCube(7, 12, 6, 2, 0, 4, tas, true, false, false);
			renderCube(9, 12, 6, 5, topElement, 4, tas, true, false, true);
		}
	}

	private void renderCube(int x, int y, int z, float w, float h, float d, TextureAtlasSprite tas, boolean renderTop, boolean renderBottom, boolean renderSides) {
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();
		
		float minVX = tas.getInterpolatedV(x);
		float maxVX = tas.getInterpolatedV(x+w);
		float minVY = tas.getInterpolatedV(y);
		float maxVY = tas.getInterpolatedV(y+h);
		
		float minUX = tas.getInterpolatedU(x);
		float maxUX = tas.getInterpolatedU(x+w);
		float minUZ = tas.getInterpolatedU(z);
		float maxUZ = tas.getInterpolatedU(z+d);
		
		float s = 1/16f;
		
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		if (renderSides) {
			vb.pos((x+0)*s, (y+h)*s, (z+0)*s).tex(minUX, maxVY).endVertex();
			vb.pos((x+w)*s, (y+h)*s, (z+0)*s).tex(maxUX, maxVY).endVertex();
			vb.pos((x+w)*s, (y+0)*s, (z+0)*s).tex(maxUX, minVY).endVertex();
			vb.pos((x+0)*s, (y+0)*s, (z+0)*s).tex(minUX, minVY).endVertex();
			
			vb.pos((x+w)*s, (y+h)*s, (z+d)*s).tex(maxUX, maxVY).endVertex();
			vb.pos((x+0)*s, (y+h)*s, (z+d)*s).tex(minUX, maxVY).endVertex();
			vb.pos((x+0)*s, (y+0)*s, (z+d)*s).tex(minUX, minVY).endVertex();
			vb.pos((x+w)*s, (y+0)*s, (z+d)*s).tex(maxUX, minVY).endVertex();
			
			
			vb.pos((x+0)*s, (y+0)*s, (z+d)*s).tex(maxUZ, minVY).endVertex();
			vb.pos((x+0)*s, (y+h)*s, (z+d)*s).tex(maxUZ, maxVY).endVertex();
			vb.pos((x+0)*s, (y+h)*s, (z+0)*s).tex(minUZ, maxVY).endVertex();
			vb.pos((x+0)*s, (y+0)*s, (z+0)*s).tex(minUZ, minVY).endVertex();
			
			vb.pos((x+w)*s, (y+0)*s, (z+0)*s).tex(minUZ, minVY).endVertex();
			vb.pos((x+w)*s, (y+h)*s, (z+0)*s).tex(minUZ, maxVY).endVertex();
			vb.pos((x+w)*s, (y+h)*s, (z+d)*s).tex(maxUZ, maxVY).endVertex();
			vb.pos((x+w)*s, (y+0)*s, (z+d)*s).tex(maxUZ, minVY).endVertex();
		}
		
		if (renderBottom) {
			vb.pos((x+0)*s, (y+0)*s, (z+0)*s).tex(minUZ, minVX).endVertex();
			vb.pos((x+w)*s, (y+0)*s, (z+0)*s).tex(minUZ, maxVX).endVertex();
			vb.pos((x+w)*s, (y+0)*s, (z+d)*s).tex(maxUZ, maxVX).endVertex();
			vb.pos((x+0)*s, (y+0)*s, (z+d)*s).tex(maxUZ, minVX).endVertex();
		}
		
		if (renderTop) {
			vb.pos((x+0)*s, (y+h)*s, (z+0)*s).tex(minUZ, minVX).endVertex();
			vb.pos((x+0)*s, (y+h)*s, (z+d)*s).tex(maxUZ, minVX).endVertex();
			vb.pos((x+w)*s, (y+h)*s, (z+d)*s).tex(maxUZ, maxVX).endVertex();
			vb.pos((x+w)*s, (y+h)*s, (z+0)*s).tex(minUZ, maxVX).endVertex();
			
		}
		tess.draw();
	}
	
}
