package com.elytradev.glasshearts;

import java.util.Collections;
import java.util.Set;

import org.apache.logging.log4j.LogManager;

import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.elytradev.glasshearts.block.BlockGlassHeart;
import com.elytradev.glasshearts.item.ItemBlockGlassHeart;
import com.elytradev.glasshearts.item.ItemGem;
import com.elytradev.glasshearts.item.ItemLifeforceBottle;
import com.elytradev.glasshearts.network.SapEffectMessage;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid="glasshearts", name="Glass Hearts", version="@VERSION@")
public class GlassHearts {
	
	@Instance
	public static GlassHearts inst;
	
	public boolean configLifeforceFromPlayerKillsOnly = false;
	public boolean configLifeforceFromUndead = false;
	
	public boolean configOverrideHealthRenderer = true;
	
	public int configGlassHeartCapacity = 1000;
	public int configLifeforceBottleSize = 333;
	public boolean configCreepersSeekHearts = true;
	public boolean configCreeperFakeExplosions = false;
	
	@SidedProxy(clientSide="com.elytradev.glasshearts.client.ClientProxy", serverSide="com.elytradev.glasshearts.CommonProxy")
	public static Proxy proxy;
	
	public NetworkContext NETWORK;
	
	public Fluid LIFEFORCE;
	public BlockFluidClassic LIFEFORCE_BLOCK;
	
	public EnchantmentSapping SAPPING;
	public SoundEvent SAP;
	
	public ItemLifeforceBottle LIFEFORCE_BOTTLE;
	public ItemGem GEM;
	
	public BlockGlassHeart GLASS_HEART;
	
	public CreativeTabs CREATIVE_TAB = new CreativeTabs("glass_heart") {
		
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(GLASS_HEART);
		}
		
	};
	
	private Invoker rayTrace = Invokers.findMethod(Item.class, null, new String[] {"func_77621_a", "rayTrace", "a"}, World.class, EntityPlayer.class, boolean.class);
	
	static {
		FluidRegistry.enableUniversalBucket();
	}
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
		
		configLifeforceFromPlayerKillsOnly = config.getBoolean("lifeforceFromPlayerKillsOnly", "Balance", false,
				  "If true, Lifeforce can only be collected from player kills,\n"
				+ "not mob kills.\n");
		configLifeforceFromUndead = config.getBoolean("lifeforceFromUndead", "Balance", false,
				  "If true, Lifeforce can be collected from undead mobs.\n"
				+ "Ignored if lifeforceFromPlayerKillsOnly is true.\n");
		
		configOverrideHealthRenderer = config.getBoolean("overrideHealthRenderer", "Compatibility", true,
				  "If true, the health bar renderer will be overridden.\n"
				+ "Required for glass outlines and sub-half-heart accuracy.\n");
		configGlassHeartCapacity = config.getInt("glassHeartCapacity", "Balance", 8000, 0, Short.MAX_VALUE,
				  "The maximum amount of Lifeforce a glass heart can hold, in mB.\n"
				+ "This directly corresponds to how much Lifeforce it takes to\n"
				+ "bring a heart container back to full, or fill a new container.\n");
		configLifeforceBottleSize = config.getInt("lifeforceBottleSize", "Balance", 333, 0, Integer.MAX_VALUE,
				  "How much Lifeforce is in a Bottle of Lifeforce, in mB.\n"
				+ "You receive a Bottle of Lifeforce when the Sapping enchant\n"
				+ "procs.\n");
		configCreepersSeekHearts = config.getBoolean("creepersSeekHearts", "Balance", true,
				  "If true, Creepers will seek out Glass Hearts and explode on\n"
				+ "them. If mobGriefing is false, the heart will still be\n"
				+ "destroyed, but no nearby blocks will be.\n");
		configCreeperFakeExplosions = config.getBoolean("creeperFakeExplosions", "Balance", false,
				  "If true, Creepers exploding on Glass Hearts will only\n"
				+ "destroy the heart, even if mobGriefing is true.");
		
		config.save();
		
		NETWORK = NetworkContext.forChannel("GlassHearts");
		NETWORK.register(SapEffectMessage.class);
		
		LIFEFORCE = new Fluid("glasshearts.lifeforce", new ResourceLocation("glasshearts", "blocks/lifeforce_still"), new ResourceLocation("glasshearts", "blocks/lifeforce_flow"));
		LIFEFORCE.setViscosity(750);
		FluidRegistry.registerFluid(LIFEFORCE);
		
		LIFEFORCE_BLOCK = new BlockFluidClassic(LIFEFORCE, Material.WATER);
		LIFEFORCE.setBlock(LIFEFORCE_BLOCK);
		
		FluidRegistry.addBucketForFluid(LIFEFORCE);
		
		LIFEFORCE_BLOCK.setRegistryName("lifeforce_block");
		GameRegistry.register(LIFEFORCE_BLOCK);
		
		
		SAPPING = new EnchantmentSapping();
		SAPPING.setRegistryName("sapping");
		GameRegistry.register(SAPPING);
		
		LIFEFORCE_BOTTLE = new ItemLifeforceBottle();
		LIFEFORCE_BOTTLE.setRegistryName("lifeforce_bottle");
		LIFEFORCE_BOTTLE.setCreativeTab(CREATIVE_TAB);
		GameRegistry.register(LIFEFORCE_BOTTLE);
		
		GEM = new ItemGem();
		GEM.setRegistryName("gem");
		GEM.setCreativeTab(CREATIVE_TAB);
		GameRegistry.register(GEM);
		
		for (int i = 0; i < ItemGem.VALID_GEMS.length; i++) {
			EnumGem gem = ItemGem.VALID_GEMS[i];
			OreDictionary.registerOre(gem.oreDictionary, new ItemStack(GEM, 1, i));
		}
		
		GLASS_HEART = new BlockGlassHeart();
		GLASS_HEART.setRegistryName("glass_heart");
		GLASS_HEART.setCreativeTab(CREATIVE_TAB);
		GameRegistry.register(GLASS_HEART);
		GameRegistry.register(new ItemBlockGlassHeart(GLASS_HEART).setRegistryName("glass_heart"));
		
		SAP = new SoundEvent(new ResourceLocation("glasshearts", "sap"));
		SAP.setRegistryName("sap");
		GameRegistry.register(SAP);
		
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(GLASS_HEART, 1, 0),
				"g g",
				"ggg",
				" g ",
				
				'g', "blockGlassColorless"));
		
		String[] dyes = {
				"Black",
				"Red",
				"Green",
				"Brown",
				"Blue",
				"Purple",
				"Cyan",
				"LightGray",
				"Gray",
				"Pink",
				"Lime",
				"Yellow",
				"LightBlue",
				"Magenta",
				"Orange",
				"White"
			};
		
		for (int i = 0; i < dyes.length; i++) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(GLASS_HEART, 1, 16-i),
					"g g",
					"ggg",
					" g ",
					
					'g', "blockGlass"+dyes[i]));
		}
		
		
		GameRegistry.registerTileEntity(TileEntityGlassHeart.class, "glasshearts:glass_heart");
		
		MinecraftForge.EVENT_BUS.register(this);
		
		proxy.onPreInit();
	}
	
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.onPostInit();
	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent e) {
		if (e.phase == Phase.START) {
			GlassHeartWorldData data = GlassHeartWorldData.getDataFor(e.world);
			Set<BlockPos> remove = Collections.emptySet();
			for (GlassHeartData ghd : data.all()) {
				if (ghd.getGem() == EnumGem.RUBY && ghd.getLifeforce() == 0 && e.world instanceof WorldServer) {
					((WorldServer)e.world).spawnParticle(EnumParticleTypes.ITEM_CRACK,
							ghd.getPos().getX()+0.5, ghd.getPos().getY()+0.5, ghd.getPos().getZ()+0.5, 32,
							0, 0, 0, 0.2, Item.getIdFromItem(GEM), 1);
					e.world.playSound(null, ghd.getPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1f, 2f);
				}
				update(ghd, e.world.getTotalWorldTime());
				if (e.world.isBlockLoaded(ghd.getPos())) {
					TileEntity te = e.world.getTileEntity(ghd.getPos());
					if (!(te instanceof TileEntityGlassHeart)) {
						LogManager.getLogger("GlassHearts").warn("Deleting orphaned Glass Heart at {}, {}, {}", ghd.getPos().getX(), ghd.getPos().getY(), ghd.getPos().getZ());
						if (remove.isEmpty()) {
							remove = Sets.newHashSet();
						}
						remove.add(ghd.getPos());
					}
				}
			}
			for (BlockPos pos : remove) {
				data.remove(pos);
			}
		}
	}
	
	public void update(IGlassHeart igh, long ticks) {
		if (igh.getLifeforceBuffer() > 0 && igh.getLifeforce() < configGlassHeartCapacity) {
			int amt = Math.min(10, Math.min(igh.getLifeforceBuffer(), configGlassHeartCapacity-igh.getLifeforce()));
			igh.setLifeforceBuffer(igh.getLifeforceBuffer()-amt);
			igh.setLifeforce(igh.getLifeforce()+amt);
		}
		if (igh.getGem() == EnumGem.OPAL) {
			if (ticks%10 == 0) {
				if (igh.getLifeforce() < configGlassHeartCapacity) {
					igh.setLifeforce(igh.getLifeforce()+1);
				}
			}
		} else if (igh.getGem() == EnumGem.RUBY && igh.getLifeforce() == 0) {
			igh.setGem(EnumGem.NONE);
			igh.setLifeforce(configGlassHeartCapacity);
		}
	}
	
	@SubscribeEvent
	public void onJoinWorld(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof EntityCreeper) {
			if (configCreepersSeekHearts) {
				EntityCreeper creeper = ((EntityCreeper)e.getEntity());
				creeper.tasks.addTask(10, new EntityAICreeperSeekHeart());
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		if (!e.getEntityLiving().world.isRemote) {
			DamageSource src = e.getSource();
			if (src.getEntity() instanceof EntityPlayer) {
				EntityPlayer p = (EntityPlayer)src.getEntity();
				ItemStack held = p.getHeldItemMainhand();
				if (configLifeforceFromPlayerKillsOnly) {
					if (!(e.getEntityLiving() instanceof EntityPlayer)) return;
				}
				if (!configLifeforceFromUndead) {
					if (e.getEntityLiving().getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
						return;
					}
				}
				int lvl = EnchantmentHelper.getEnchantmentLevel(SAPPING, held);
				if (lvl > 0) {
					if (p.world.rand.nextInt(10) < lvl) {
						int amt = p.inventory.clearMatchingItems(Items.GLASS_BOTTLE, 0, 1, null);
						if (amt == 1) {
							if (!p.inventory.addItemStackToInventory(new ItemStack(LIFEFORCE_BOTTLE))) {
								p.dropItem(LIFEFORCE_BOTTLE, 1);
							}
							p.world.playSound(null, p.posX, p.posY, p.posZ, SAP, SoundCategory.NEUTRAL, 1f, 1f);
							new SapEffectMessage(e.getEntityLiving(), p).sendToAllWatching(e.getEntityLiving());
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRightClick(RightClickItem e) {
		World world = e.getWorld();
		EntityPlayer player = e.getEntityPlayer();
		ItemStack stack = e.getItemStack();
		if (stack.getItem() instanceof ItemGlassBottle) {
			RayTraceResult rtr = (RayTraceResult)rayTrace.invoke(e.getItemStack().getItem(), world, player, true);
	
			if (rtr != null) {
				if (rtr.typeOfHit == RayTraceResult.Type.BLOCK) {
					BlockPos pos = rtr.getBlockPos();

					if (world.isBlockModifiable(player, pos) && player.canPlayerEdit(pos.offset(rtr.sideHit), rtr.sideHit, stack)) {
						if (world.getBlockState(pos).getBlock() == LIFEFORCE_BLOCK) {
							world.playSound(player, player.posX, player.posY, player.posZ, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 0.75f, 1f);
							world.setBlockToAir(pos);
							e.setResult(Result.DENY);
							e.getItemStack().shrink(1);
							ItemStack res = new ItemStack(LIFEFORCE_BOTTLE);
							if (!player.inventory.addItemStackToInventory(res)) {
								player.dropItem(res, false);
							}
						}
					}
				}
			}
		}
	}
	
	public static void sendUpdatePacket(TileEntity te) {
		sendUpdatePacket(te, te.getUpdateTag());
	}
	
	public static void sendUpdatePacket(TileEntity te, NBTTagCompound nbt) {
		if (!te.hasWorld() || te.getWorld().isRemote) return;
		WorldServer ws = (WorldServer)te.getWorld();
		Chunk c = te.getWorld().getChunkFromBlockCoords(te.getPos());
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(te.getPos(), te.getBlockMetadata(), nbt);
		for (EntityPlayerMP player : te.getWorld().getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue())) {
			if (ws.getPlayerChunkMap().isPlayerWatchingChunk(player, c.xPosition, c.zPosition)) {
				player.connection.sendPacket(packet);
			}
		}
	}
	
}
