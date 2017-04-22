package com.elytradev.glasshearts;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.elytradev.glasshearts.block.BlockGlassHeart;
import com.elytradev.glasshearts.block.BlockOre;
import com.elytradev.glasshearts.block.BlockPetrifiedLog;
import com.elytradev.glasshearts.capability.CapabilityHealthHandler;
import com.elytradev.glasshearts.capability.EntityHealthHandler;
import com.elytradev.glasshearts.capability.IHealthHandler;
import com.elytradev.glasshearts.enchant.EnchantmentSapping;
import com.elytradev.glasshearts.entity.EntityAICreeperSeekHeart;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.item.ItemBlockGlassHeart;
import com.elytradev.glasshearts.item.ItemBlockOre;
import com.elytradev.glasshearts.item.ItemGem;
import com.elytradev.glasshearts.item.ItemLifeforceBottle;
import com.elytradev.glasshearts.item.ItemStaff;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.elytradev.glasshearts.logic.PlayerHandler;
import com.elytradev.glasshearts.network.ParticleEffectMessage;
import com.elytradev.glasshearts.network.PlayHeartEffectMessage;
import com.elytradev.glasshearts.network.UpdateHeartsMessage;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;
import com.elytradev.glasshearts.world.GenerateGems;
import com.elytradev.glasshearts.world.GlassHeartData;
import com.elytradev.glasshearts.world.GlassHeartWorldData;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid="glasshearts", name="Glass Hearts", version="@VERSION@")
public class GlassHearts {
	
	@Instance
	public static GlassHearts inst;
	
	public static final Logger LOG = LogManager.getLogger("GlassHearts");
	
	public boolean configLifeforceFromPlayerKillsOnly = false;
	public boolean configLifeforceFromUndead = false;
	
	public boolean configOverrideHealthRenderer = true;
	
	public int configGlassHeartCapacity = 1000;
	public int configLifeforceBottleSize = 400;
	public int configGlassHeartFillRate = 10;
	public boolean configCreepersSeekHearts = true;
	public boolean configCreeperFakeExplosions = false;
	
	@SidedProxy(clientSide="com.elytradev.glasshearts.client.ClientProxy", serverSide="com.elytradev.glasshearts.CommonProxy")
	public static Proxy proxy;
	
	public NetworkContext NETWORK;
	
	public Fluid LIFEFORCE;
	public BlockFluidClassic LIFEFORCE_BLOCK;
	
	public EnchantmentSapping SAPPING;
	
	public SoundEvent SAP;
	public SoundEvent ATTUNE;
	
	public ItemLifeforceBottle LIFEFORCE_BOTTLE;
	public ItemGem GEM;
	public ItemStaff STAFF;
	
	public BlockGlassHeart GLASS_HEART;
	public BlockOre ORE;
	public BlockPetrifiedLog PETRIFIED_LOG;
	
	public IStatType millibucketStatType = mb -> {
		if (mb > 1000) {
			return (mb/1000)+"."+((mb/100)%10)+"B";
		}
		return mb+"mB";
	};
	
	public StatBase LIFEFORCE_CONSUMED = new StatBasic("glasshearts:stat.lifeforce_consumed", new TextComponentTranslation("stat.glasshearts.lifeforce_consumed"), millibucketStatType)
			.initIndependentStat()
			.registerStat();
	
	public StatBase LIFEFORCE_COLLECTED = new StatBasic("glasshearts:stat.lifeforce_collected", new TextComponentTranslation("stat.glasshearts.lifeforce_collected"), millibucketStatType)
			.initIndependentStat()
			.registerStat();
	
	public StatBase HEALTH_TRANSFERRED = new StatBasic("glasshearts:stat.health_transferred", new TextComponentTranslation("stat.glasshearts.health_transferred"), StatBase.simpleStatType)
			.initIndependentStat()
			.registerStat();
	
	public CreativeTabs CREATIVE_TAB = new CreativeTabs("glass_heart") {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(GLASS_HEART);
		}
	};
	
	private Invoker rayTrace = Invokers.findMethod(Item.class, null, new String[] {"func_77621_a", "rayTrace", "a"}, World.class, EntityPlayer.class, boolean.class);
	
	private Map<EntityPlayer, PlayerHandler> playerHandlers = new WeakHashMap<>();
	
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
		configLifeforceBottleSize = config.getInt("lifeforceBottleSize", "Balance", 400, 0, Integer.MAX_VALUE,
				  "How much Lifeforce is in a Bottle of Lifeforce, in mB.\n"
				+ "You receive a Bottle of Lifeforce when the Sapping enchant\n"
				+ "procs.\n");
		configGlassHeartFillRate = config.getInt("glassHeartFillRate", "Balance", 10, 1, Short.MAX_VALUE,
				  "The maximum amount of Lifeforce transferred from the buffer\n"
				+ "tank to the main tank every tick. Keeping this low prevents\n"
				+ "players from becoming invincible by refilling their glass\n"
				+ "hearts quickly.");
		configCreepersSeekHearts = config.getBoolean("creepersSeekHearts", "Balance", true,
				  "If true, Creepers will seek out Glass Hearts and explode on\n"
				+ "them. If mobGriefing is false, the heart will still be\n"
				+ "destroyed, but no nearby blocks will be.\n");
		configCreeperFakeExplosions = config.getBoolean("creeperFakeExplosions", "Balance", false,
				  "If true, Creepers exploding on Glass Hearts will only\n"
				+ "destroy the heart, even if mobGriefing is true.");
		
		config.save();
		
		// short channel name since update messages might be really frequent
		NETWORK = NetworkContext.forChannel("gh");
		NETWORK.register(ParticleEffectMessage.class);
		NETWORK.register(UpdateHeartsMessage.class);
		NETWORK.register(PlayHeartEffectMessage.class);
		
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
		
		STAFF = new ItemStaff();
		STAFF.setRegistryName("staff");
		STAFF.setCreativeTab(CREATIVE_TAB);
		GameRegistry.register(STAFF);
		
		GLASS_HEART = new BlockGlassHeart();
		GLASS_HEART.setRegistryName("glass_heart");
		GLASS_HEART.setCreativeTab(CREATIVE_TAB);
		GLASS_HEART.setUnlocalizedName("glasshearts.glass_heart");
		GameRegistry.register(GLASS_HEART);
		GameRegistry.register(new ItemBlockGlassHeart(GLASS_HEART).setRegistryName("glass_heart"));
		
		ORE = new BlockOre();
		ORE.setRegistryName("ore");
		ORE.setCreativeTab(CREATIVE_TAB);
		ORE.setUnlocalizedName("glasshearts.ore");
		GameRegistry.register(ORE);
		GameRegistry.register(new ItemBlockOre(ORE).setRegistryName("ore"));
		
		PETRIFIED_LOG = new BlockPetrifiedLog();
		PETRIFIED_LOG.setRegistryName("petrified_log");
		PETRIFIED_LOG.setCreativeTab(CREATIVE_TAB);
		PETRIFIED_LOG.setUnlocalizedName("glasshearts.petrified_log");
		GameRegistry.register(PETRIFIED_LOG);
		GameRegistry.register(new ItemBlock(PETRIFIED_LOG).setRegistryName("petrified_log"));
		
		SAP = new SoundEvent(new ResourceLocation("glasshearts", "sap"));
		SAP.setRegistryName("sap");
		GameRegistry.register(SAP);
		
		ATTUNE = new SoundEvent(new ResourceLocation("glasshearts", "attune"));
		ATTUNE.setRegistryName("attune");
		GameRegistry.register(ATTUNE);
		
		GameRegistry.registerWorldGenerator(new GenerateGems(), 2);
		
		
		GameRegistry.addRecipe(new ShapedOreRecipe(STAFF,
				"  o",
				" //",
				"/  ",
				
				'/', Items.BLAZE_ROD,
				'o', "gemOpal"));
		
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
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onLogIn(PlayerLoggedInEvent e) {
		PlayerHandler ph = new PlayerHandler(e.player);
		playerHandlers.put(e.player, ph);
		ph.resync();
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onRespawn(PlayerRespawnEvent e) {
		PlayerHandler ph = new PlayerHandler(e.player);
		playerHandlers.put(e.player, ph);
		ph.resync();
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onHurt(LivingHurtEvent e) {
		float amt = e.getAmount();
		if (amt > e.getEntityLiving().getAbsorptionAmount()) {
			amt -= e.getEntityLiving().getAbsorptionAmount();
			if (e.getEntityLiving().hasCapability(CapabilityHealthHandler.CAPABILITY, null)) {
				IHealthHandler cap = e.getEntityLiving().getCapability(CapabilityHealthHandler.CAPABILITY, null);
				cap.damage(amt/2f, e.getSource());
			}
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onHeal(LivingHealEvent e) {
		if (e.getEntityLiving().hasCapability(CapabilityHealthHandler.CAPABILITY, null)) {
			IHealthHandler cap = e.getEntityLiving().getCapability(CapabilityHealthHandler.CAPABILITY, null);
			cap.heal(e.getAmount()/2f);
		}
	}
	
	@SubscribeEvent
	public void onPlayerAttachCapabilities(AttachCapabilitiesEvent<Entity> e) {
		if (e.getObject() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)e.getObject();
			EntityHealthHandler bhh = new EntityHealthHandler(player);
			e.addCapability(new ResourceLocation("glasshearts", "health"), new ICapabilityProvider() {
				
				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return facing == null && capability == CapabilityHealthHandler.CAPABILITY;
				}
				
				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					if (capability == CapabilityHealthHandler.CAPABILITY && facing == null) {
						return (T)bhh;
					}
					return null;
				}
			});
		}
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent e) {
		for (EntityPlayer ep : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
			PlayerHandler ph = playerHandlers.get(ep);
			if (ph == null) {
				ph = new PlayerHandler(ep);
				playerHandlers.put(ep, ph);
			}
			if (e.phase == Phase.START) {
				ph.preTick();
			} else {
				ph.postTick();
			}
		}
	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent e) {
		if (e.side != Side.SERVER) return;
		if (e.phase == Phase.START) {
			GlassHeartWorldData data = GlassHeartWorldData.getDataFor(e.world);
			Set<BlockPos> remove = Collections.emptySet();
			for (GlassHeartData ghd : data.all()) {
				if (ghd.getGem() == EnumGem.RUBY && ghd.getLifeforce() == 0 && ghd.hasBeenFull() && e.world instanceof WorldServer) {
					((WorldServer)e.world).spawnParticle(EnumParticleTypes.ITEM_CRACK,
							ghd.getHeartPos().getX()+0.5, ghd.getHeartPos().getY()+0.5, ghd.getHeartPos().getZ()+0.5, 32,
							0, 0, 0, 0.2, Item.getIdFromItem(GEM), 1);
					e.world.playSound(null, ghd.getHeartPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1f, 2f);
				}
				update(ghd, e.world.getTotalWorldTime());
				if (e.world.isBlockLoaded(ghd.getHeartPos())) {
					TileEntity te = e.world.getTileEntity(ghd.getHeartPos());
					if (!(te instanceof TileEntityGlassHeart)) {
						LOG.warn("Deleting orphaned Glass Heart at {}, {}, {}", ghd.getHeartPos().getX(), ghd.getHeartPos().getY(), ghd.getHeartPos().getZ());
						if (remove.isEmpty()) {
							remove = Sets.newHashSet();
						}
						remove.add(ghd.getHeartPos());
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
			int amt = Math.min(configGlassHeartFillRate, Math.min(igh.getLifeforceBuffer(), configGlassHeartCapacity-igh.getLifeforce()));
			igh.setLifeforceBuffer(igh.getLifeforceBuffer()-amt);
			igh.setLifeforce(igh.getLifeforce()+amt);
		}
		if (igh.getGem().getState(igh) != EnumGemState.INACTIVE) {
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
		if (igh.getLifeforce() == 0) {
			igh.setHasBeenFull(false);
		}
		if (igh.getLifeforce() == configGlassHeartCapacity) {
			igh.setHasBeenFull(true);
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
							p.addStat(LIFEFORCE_COLLECTED, configLifeforceBottleSize);
							if (!p.inventory.addItemStackToInventory(new ItemStack(LIFEFORCE_BOTTLE))) {
								p.dropItem(LIFEFORCE_BOTTLE, 1);
							}
							p.world.playSound(null, p.posX, p.posY, p.posZ, SAP, SoundCategory.NEUTRAL, 1f, 1f);
							EntityLivingBase el = e.getEntityLiving();
							new ParticleEffectMessage(el.posX, el.posY, el.posZ, p, 0).sendToAllWatching(el);
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
		Thread.dumpStack();
		System.out.println("SYNC "+te.getPos().getX()+", "+te.getPos().getY()+", "+te.getPos().getZ());
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
