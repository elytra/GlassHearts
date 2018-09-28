package com.elytradev.glasshearts.client;

import java.util.Iterator;
import java.util.List;

import com.elytradev.glasshearts.CommonProxy;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.client.guiparticle.GuiParticle;
import com.elytradev.glasshearts.enums.EnumGemOre;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.gem.Gem;
import com.elytradev.glasshearts.init.Gems;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientProxy extends CommonProxy {
	
	public HeartRenderer heartRenderer = new HeartRenderer();
	private List<TileEntityGlassHeart> glassHearts = Lists.newArrayList();
	public List<GuiParticle> guiParticles = Lists.newArrayList();
	
	public static int ticks;
	
	@Override
	public void onPreInit() {
		super.onPreInit();
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGlassHeart.class, new RenderGlassHeart());
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onModelRegister(ModelRegistryEvent e) {
		ModelLoader.setCustomModelResourceLocation(GlassHearts.inst.LIFEFORCE_BOTTLE, 0, new ModelResourceLocation("glasshearts:lifeforce_bottle#inventory"));
		ModelLoader.setCustomModelResourceLocation(GlassHearts.inst.STAFF, 0, new ModelResourceLocation("glasshearts:staff#inventory"));
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.GLASS_HEART), 0, new ModelResourceLocation("glasshearts:glass_heart#inventory"));
		for (int i = 1; i < 17; i++) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.GLASS_HEART), i, new ModelResourceLocation("glasshearts:stained_glass_heart#inventory"));
		}
		
		for (EnumGemOre gem : EnumGemOre.VALUES) {
			ModelLoader.setCustomModelResourceLocation(GlassHearts.inst.GEM, gem.ordinal(), new ModelResourceLocation("glasshearts:"+gem.getName()+"#inventory"));
			if (gem != EnumGemOre.AMBER) {
				ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.ORE), gem.ordinalWithoutAmber(), new ModelResourceLocation("glasshearts:ore#variant="+gem.getName()));
			}
		}
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GlassHearts.inst.PETRIFIED_LOG), 0, new ModelResourceLocation("glasshearts:petrified_log#axis=y"));
	}
	
	@Override
	public void onPostInit() {
		super.onPostInit();

		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) {
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
				Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
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
	public void onTooltip(ItemTooltipEvent e) {
		Gem gem = Gem.fromItemStack(e.getItemStack());
		if (gem != Gems.NONE) {
			String swp = null;
			if (e.getFlags().isAdvanced()) {
				swp = e.getToolTip().remove(e.getToolTip().size()-1);
			}
			if (I18n.hasKey("tooltip.glasshearts.when_heart_adorned."+gem.getRegistryName().getResourcePath())) {
				e.getToolTip().add("\u00A75"+I18n.format("tooltip.glasshearts.when_heart_adorned"));
				String s = I18n.format("tooltip.glasshearts.when_heart_adorned."+gem.getRegistryName().getResourcePath());
				for (String l : Splitter.on("\\n").split(s)) {
					e.getToolTip().add("  \u00A79"+l);
				}
			}
			if (I18n.hasKey("tooltip.glasshearts.when_heart_emptied."+gem.getRegistryName().getResourcePath())) {
				e.getToolTip().add("\u00A75"+I18n.format("tooltip.glasshearts.when_heart_emptied"));
				String s = I18n.format("tooltip.glasshearts.when_heart_emptied."+gem.getRegistryName().getResourcePath());
				for (String l : Splitter.on("\\n").split(s)) {
					e.getToolTip().add("  \u00A79"+l);
				}
			}
			if (e.getFlags().isAdvanced() && swp != null) {
				e.getToolTip().add(swp);
			}
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		if (e.phase == Phase.START) {
			if (!Minecraft.getMinecraft().isGamePaused()) {
				ticks++;
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
							if (tegh.getGem() != Gems.NONE && tegh.getGem().getState(tegh) != EnumGemState.INACTIVE) {
								float yaw = (float)Math.toRadians(RenderGlassHeart.getAnimTime(tegh, 0)%360);
								Vec3d base = new Vec3d(tegh.getHeartPos()).addVector(0.5, 0.565, 0.5);
								float dist = 0.3f;
								Vec3d dir = new Vec3d(MathHelper.sin(yaw), 0, MathHelper.cos(yaw)).scale(dist);
								
								Vec3d aPos = base.add(dir);
								Vec3d bPos = base.subtract(dir);
								
								ParticleRedstone a = new ParticleRedstone(Minecraft.getMinecraft().world, aPos.x, aPos.y, aPos.z, 1f, 1f, 1f, 1f) {};
								ParticleRedstone b = new ParticleRedstone(Minecraft.getMinecraft().world, bPos.x, bPos.y, bPos.z, 1f, 1f, 1f, 1f) {};
								
								int gemColor = tegh.getGem().getColor();
								
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
			} else {
				glassHearts.clear();
			}
		}
	}
	
}
