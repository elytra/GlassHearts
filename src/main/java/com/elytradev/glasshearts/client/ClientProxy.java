package com.elytradev.glasshearts.client;

import java.util.Iterator;
import java.util.List;

import com.elytradev.glasshearts.CommonProxy;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.block.BlockOre;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.item.ItemGem;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientProxy extends CommonProxy {
	
	public HeartRenderer heartRenderer = new HeartRenderer();
	private List<TileEntityGlassHeart> glassHearts = Lists.newArrayList();
	public List<GuiParticle> guiParticles = Lists.newArrayList();
	
	@Override
	public void onPreInit() {
		super.onPreInit();
		ModelLoader.setCustomModelResourceLocation(GlassHearts.inst.LIFEFORCE_BOTTLE, 0, new ModelResourceLocation("glasshearts:lifeforce_bottle#inventory"));
		ModelLoader.setCustomModelResourceLocation(GlassHearts.inst.STAFF, 0, new ModelResourceLocation("glasshearts:staff#inventory"));
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.GLASS_HEART), 0, new ModelResourceLocation("glasshearts:glass_heart#inventory"));
		for (int i = 1; i < 17; i++) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.GLASS_HEART), i, new ModelResourceLocation("glasshearts:stained_glass_heart#inventory"));
		}
		
		for (int i = 0; i < ItemGem.VALID_GEMS.length; i++) {
			ModelLoader.setCustomModelResourceLocation(GlassHearts.inst.GEM, i, new ModelResourceLocation("glasshearts:"+ItemGem.VALID_GEMS[i].getName()+"#inventory"));
		}
		
		for (int i = 0; i < BlockOre.VALID_GEMS.length; i++) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.ORE), i, new ModelResourceLocation("glasshearts:ore#variant="+BlockOre.VALID_GEMS[i].getName()));
		}
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.PETRIFIED_LOG), 0, new ModelResourceLocation("glasshearts:petrified_log#axis=y"));
		
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
	public void onPreRender(RenderGameOverlayEvent.Pre e) {
		if (e.getType() == ElementType.HEALTH) {
			if (GlassHearts.inst.configOverrideHealthRenderer && !heartRenderer.containers.isEmpty()) {
				e.setCanceled(true);
				heartRenderer.renderHealth(e.getResolution(), e.getPartialTicks());
			}
		}
	}
	
	@SubscribeEvent
	public void onPostRender(RenderGameOverlayEvent.Post e) {
		if (e.getType() == ElementType.ALL) {
			Minecraft.getMinecraft().mcProfiler.startSection("glasshearts:guiparticlesrender");
			for (GuiParticle gp : guiParticles) {
				if (gp.posY > e.getResolution().getScaledHeight()+24 || gp.posX > e.getResolution().getScaledWidth()+24 ||
						gp.posX < -24) {
					gp.setExpired();
				}
				gp.render(e.getPartialTicks());
			}
			Minecraft.getMinecraft().mcProfiler.endSection();
		}
	}
	
	@Override
	public void onLoad(TileEntityGlassHeart te) {
		super.onLoad(te);
		if (te.hasWorld() && te.getWorld().isRemote) {
			te.setClient(true);
			glassHearts.add(te);
		}
	}
	
	@SubscribeEvent
	public void onText(RenderGameOverlayEvent.Text e) {
		for (int i = 0; i < e.getLeft().size(); i++) {
			if (e.getLeft().get(i).startsWith("P:")) {
				e.getLeft().set(i, e.getLeft().get(i)+", GuiP: "+guiParticles.size());
				break;
			}
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		if (e.phase == Phase.START ) {
			if (!Minecraft.getMinecraft().isGamePaused()) {
				Minecraft.getMinecraft().mcProfiler.startSection("glasshearts:guiparticlesupdate"); {
					Iterator<GuiParticle> iter = guiParticles.iterator();
					while (iter.hasNext()) {
						GuiParticle gp = iter.next();
						gp.update();
						if (gp.expired) {
							iter.remove();
						}
					}
				} Minecraft.getMinecraft().mcProfiler.endSection();
			}
			if (Minecraft.getMinecraft().world != null) {
				EntityPlayer player = Minecraft.getMinecraft().player;
				heartRenderer.tick();
				Iterator<TileEntityGlassHeart> iter = glassHearts.iterator();
				while (iter.hasNext()) {
					TileEntityGlassHeart tegh = iter.next();
					if (tegh.isInvalid()) {
						iter.remove();
					} else {
						GlassHearts.inst.update(tegh, tegh.getWorld().getTotalWorldTime());
						if (!Minecraft.getMinecraft().isGamePaused() && tegh.getDistanceSq(player.posX, player.posY, player.posZ) < 384) {
							if (tegh.getGem() != EnumGem.NONE && tegh.getGem().getState(tegh) != EnumGemState.INACTIVE) {
								float yaw = (float)Math.toRadians(RenderGlassHeart.getAnimTime(tegh, 0)%360);
								Vec3d base = new Vec3d(tegh.getPos()).addVector(0.5, 0.565, 0.5);
								float dist = 0.3f;
								Vec3d dir = new Vec3d(MathHelper.sin(yaw), 0, MathHelper.cos(yaw)).scale(dist);
								
								Vec3d aPos = base.add(dir);
								Vec3d bPos = base.subtract(dir);
								
								ParticleRedstone a = new ParticleRedstone(Minecraft.getMinecraft().world, aPos.xCoord, aPos.yCoord, aPos.zCoord, 1f, 1f, 1f, 1f) {};
								ParticleRedstone b = new ParticleRedstone(Minecraft.getMinecraft().world, bPos.xCoord, bPos.yCoord, bPos.zCoord, 1f, 1f, 1f, 1f) {};
								
								int gemColor = tegh.getGem().color;
								
								float c1 = (player.world.rand.nextFloat()-0.5f)/8;
								float c2 = (player.world.rand.nextFloat()-0.5f)/8;
								
								a.setRBGColorF((((gemColor>>16)&0xFF)/255f)+c1, (((gemColor>>8)&0xFF)/255f)+c1, ((gemColor&0xFF)/255f)+c1);
								b.setRBGColorF((((gemColor>>16)&0xFF)/255f)+c2, (((gemColor>>8)&0xFF)/255f)+c2, ((gemColor&0xFF)/255f)+c2);
								
								Minecraft.getMinecraft().effectRenderer.addEffect(a);
								Minecraft.getMinecraft().effectRenderer.addEffect(b);
							}						
						}
					}
				}
			}
		}
	}
	
}
