package com.elytradev.glasshearts.client;

import com.elytradev.glasshearts.CommonProxy;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.item.ItemGem;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientProxy extends CommonProxy {
	
	private HeartRenderer heartRenderer = new HeartRenderer();
	
	@Override
	public void onPreInit() {
		super.onPreInit();
		ModelLoader.setCustomModelResourceLocation(GlassHearts.inst.LIFEFORCE_BOTTLE, 0, new ModelResourceLocation("glasshearts:lifeforce_bottle#inventory"));
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.GLASS_HEART), 0, new ModelResourceLocation("glasshearts:glass_heart#inventory"));
		for (int i = 1; i < 17; i++) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.GLASS_HEART), i, new ModelResourceLocation("glasshearts:stained_glass_heart#inventory"));
		}
		
		for (int i = 0; i < ItemGem.VALID_GEMS.length; i++) {
			ModelLoader.setCustomModelResourceLocation(GlassHearts.inst.GEM, i, new ModelResourceLocation("glasshearts:"+ItemGem.VALID_GEMS[i].getName()+"#inventory"));
		}
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGlassHeart.class, new RenderGlassHeart());
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onPostInit() {
		super.onPostInit();
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if (stack.getMetadata() == 0) {
					return -1;
				}
				float[] fleece = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(stack.getMetadata()-1));
				int color = (((int)(fleece[0]*255)&0xFF) << 16)|
						(((int)(fleece[1]*255)&0xFF) << 8)|
						((int)(fleece[2]*255)&0xFF);
				return color;
			}
		}, GlassHearts.inst.GLASS_HEART);
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Pre e) {
		if (e.getType() == ElementType.HEALTH) {
			if (GlassHearts.inst.configOverrideHealthRenderer) {
				e.setCanceled(true);
				heartRenderer.renderHealth(e.getResolution(), e.getPartialTicks());
			}
		} else if (e.getType() == ElementType.HEALTHMOUNT) {
			if (GlassHearts.inst.configOverrideHealthRenderer && heartRenderer.useMoreSpace()) {
				e.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		if (e.phase == Phase.START && Minecraft.getMinecraft().world != null) {
			heartRenderer.tick();
			// this effect is actually really annoying
			if (Integer.valueOf(4).intValue() == 4) return;
			World world = Minecraft.getMinecraft().world;
			EntityPlayer player = Minecraft.getMinecraft().player;
			int baseX = (int)(player.posX/16);
			int baseZ = (int)(player.posZ/16);
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					Chunk c = world.getChunkFromChunkCoords(baseX+x, baseZ+z);
					for (TileEntity te : c.getTileEntityMap().values()) {
						if (te instanceof TileEntityGlassHeart && player.getDistanceSqToCenter(te.getPos()) < 25) {
							double teX = te.getPos().getX()+0.5;
							double teY = te.getPos().getY()+0.5;
							double teZ = te.getPos().getZ()+0.5;
							float r = 0.05f;
							for (int i = 0; i < 16; i++) {
								float pX = (float)(world.rand.nextGaussian());
								float pY = (float)(world.rand.nextGaussian());
								float pZ = (float)(world.rand.nextGaussian());

								ParticleRedstoneSeekEntity p = new ParticleRedstoneSeekEntity(player, world, teX+(r*pX), teY+(r*pY), teZ+(r*pZ), 1, 0.8f, 0, 0);
								p.setVelocity(x/128, pY/128, pZ/128);
								Minecraft.getMinecraft().effectRenderer.addEffect(p);
							}
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent(receiveCanceled=true)
	public void onRenderFood(RenderGameOverlayEvent.Pre e) {
		if (e.getType() == ElementType.FOOD) {
			heartRenderer.setUseMoreSpace(e.isCanceled());
		}
	}
	
}
