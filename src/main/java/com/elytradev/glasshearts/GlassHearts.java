package com.elytradev.glasshearts;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.elytradev.glasshearts.block.BlockFluidLifeforce;
import com.elytradev.glasshearts.block.BlockGlassHeart;
import com.elytradev.glasshearts.block.BlockOre;
import com.elytradev.glasshearts.block.BlockPetrifiedLog;
import com.elytradev.glasshearts.capability.CapabilityHeartHandler;
import com.elytradev.glasshearts.capability.EntityHeartHandler;
import com.elytradev.glasshearts.capability.IHeartHandler;
import com.elytradev.glasshearts.enchant.EnchantmentSapping;
import com.elytradev.glasshearts.entity.EntityAICreeperSeekHeart;
import com.elytradev.glasshearts.enums.EnumGemOre;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.gem.Gem;
import com.elytradev.glasshearts.init.Gems;
import com.elytradev.glasshearts.item.ItemBlockGlassHeart;
import com.elytradev.glasshearts.item.ItemBlockOre;
import com.elytradev.glasshearts.item.ItemGem;
import com.elytradev.glasshearts.item.ItemLifeforceBottle;
import com.elytradev.glasshearts.item.ItemStaff;
import com.elytradev.glasshearts.logic.BlockHeartContainerOwner;
import com.elytradev.glasshearts.logic.HeartContainer;
import com.elytradev.glasshearts.logic.HeartContainerOwner;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.elytradev.glasshearts.logic.PlayerHandler;
import com.elytradev.glasshearts.network.ParticleEffectMessage;
import com.elytradev.glasshearts.network.PlayHeartEffectMessage;
import com.elytradev.glasshearts.network.UpdateHeartsMessage;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;
import com.elytradev.glasshearts.world.GenerateGems;
import com.elytradev.glasshearts.world.GeneratePetrifiedTree;
import com.elytradev.glasshearts.world.GlassHeartData;
import com.elytradev.glasshearts.world.GlassHeartWorldData;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
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
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
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
	public int configMaxContainers = 40;
	public boolean configCreepersSeekHearts = true;
	public boolean configCreeperFakeExplosions = false;
	
	public boolean configGeneratePetrifiedTrees = true;
	
	public Set<EnumGemOre> configGenerateGems = EnumSet.allOf(EnumGemOre.class);
	
	@SidedProxy(clientSide="com.elytradev.glasshearts.client.ClientProxy", serverSide="com.elytradev.glasshearts.CommonProxy")
	public static Proxy proxy;
	
	public NetworkContext NETWORK;
	
	public Fluid LIFEFORCE;
	public BlockFluidLifeforce LIFEFORCE_BLOCK;
	
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
	
	private Invoker rayTrace = Invokers.findMethod(Item.class, "rayTrace", "func_77621_a", World.class, EntityPlayer.class, boolean.class);
	private Invoker applyArmorCalculations = Invokers.findMethod(EntityLivingBase.class, "applyArmorCalculations", "func_70655_b", DamageSource.class, float.class);
	private Invoker applyPotionDamageCalculations = Invokers.findMethod(EntityLivingBase.class, "applyPotionDamageCalculations", "func_70672_c", DamageSource.class, float.class);
	
	private Invoker explode = Invokers.findMethod(EntityCreeper.class, "explode", "func_146077_cc");
	
	private Map<EntityPlayer, PlayerHandler> playerHandlers = new WeakHashMap<>();

	public static final ResourceLocation TEX = new ResourceLocation("glasshearts", "textures/gui/heart.png");
	
	static {
		FluidRegistry.enableUniversalBucket();
	}
	
	public GlassHearts() {
		MinecraftForge.EVENT_BUS.register(this);
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
				+ "hearts quickly.\n");
		configMaxContainers = config.getInt("maxContainers", "Balance", 40, 1, Short.MAX_VALUE,
				  "The maximum amount of heart containers one player can have.\n"
				+ "This includes the 10 vanilla hearts.\n");
		configCreepersSeekHearts = config.getBoolean("creepersSeekHearts", "Balance", true,
				  "If true, Creepers will seek out Glass Hearts and explode on\n"
				+ "them. If mobGriefing is false, the heart will still be\n"
				+ "destroyed, but no nearby blocks will be.\n");
		configCreeperFakeExplosions = config.getBoolean("creeperFakeExplosions", "Balance", false,
				  "If true, Creepers exploding on Glass Hearts will only\n"
				+ "destroy the heart, even if mobGriefing is true.\n");
		
		configGeneratePetrifiedTrees = config.getBoolean("generatePetrifiedTrees", "World", true,
				  "If true, Petrified Trees will generate in forests, which can\n"
				+ "be broken for amber and sticks.\n");
		
		configGenerateGems.clear();
		if (config.getBoolean("generateAmethystOre", "World", true, "")) {
			configGenerateGems.add(EnumGemOre.AMETHYST);
		}
		if (config.getBoolean("generateRubyOre", "World", true, "")) {
			configGenerateGems.add(EnumGemOre.RUBY);
		}
		if (config.getBoolean("generateTopazOre", "World", true, "")) {
			configGenerateGems.add(EnumGemOre.TOPAZ);
		}
		if (config.getBoolean("generateSapphireOre", "World", true, "")) {
			configGenerateGems.add(EnumGemOre.SAPPHIRE);
		}
		if (config.getBoolean("generateOpalOre", "World", true, "")) {
			configGenerateGems.add(EnumGemOre.OPAL);
		}
		if (config.getBoolean("generateOnyxOre", "World", true, "")) {
			configGenerateGems.add(EnumGemOre.ONYX);
		}
		if (config.getBoolean("generateAgateOre", "World", true, "")) {
			configGenerateGems.add(EnumGemOre.AGATE);
		}
		
		config.save();
		
		// short channel name since update messages might be really frequent
		NETWORK = NetworkContext.forChannel("gh");
		NETWORK.register(ParticleEffectMessage.class);
		NETWORK.register(UpdateHeartsMessage.class);
		NETWORK.register(PlayHeartEffectMessage.class);
		
		CapabilityHeartHandler.register();
		Gem.registerGems();
		
		LIFEFORCE = new Fluid("glasshearts.lifeforce", new ResourceLocation("glasshearts", "blocks/lifeforce_still"), new ResourceLocation("glasshearts", "blocks/lifeforce_flow"));
		LIFEFORCE.setViscosity(750);
		FluidRegistry.registerFluid(LIFEFORCE);
		
		FluidRegistry.addBucketForFluid(LIFEFORCE);
		
		
		
		GameRegistry.registerWorldGenerator(new GenerateGems(), 2);
		
		
		
		GameRegistry.registerTileEntity(TileEntityGlassHeart.class, "glasshearts:glass_heart");
		
		//if (Loader.isModLoaded("tconstruct")) {
		//	TConIntegration.init();
		//}
		
		proxy.onPreInit();
	}
	
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent e) {
		e.registerServerCommand(new CommandHeartInfo());
	}
	
	@SubscribeEvent
	public void onRegisterEnchantments(RegistryEvent.Register<Enchantment> e) {
		SAPPING = new EnchantmentSapping();
		SAPPING.setRegistryName("sapping");
		e.getRegistry().register(SAPPING);
	}
	
	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> e) {
		LIFEFORCE_BOTTLE = new ItemLifeforceBottle();
		LIFEFORCE_BOTTLE.setRegistryName("lifeforce_bottle");
		LIFEFORCE_BOTTLE.setCreativeTab(CREATIVE_TAB);
		e.getRegistry().register(LIFEFORCE_BOTTLE);
		
		GEM = new ItemGem();
		GEM.setRegistryName("gem");
		GEM.setCreativeTab(CREATIVE_TAB);
		e.getRegistry().register(GEM);
		
		STAFF = new ItemStaff();
		STAFF.setRegistryName("staff");
		STAFF.setCreativeTab(CREATIVE_TAB);
		e.getRegistry().register(STAFF);
		
		e.getRegistry().register(new ItemBlockGlassHeart(GLASS_HEART).setRegistryName("glass_heart"));
		e.getRegistry().register(new ItemBlockOre(ORE).setRegistryName("ore"));
		e.getRegistry().register(new ItemBlock(PETRIFIED_LOG).setRegistryName("petrified_log"));
		
		for (int i = 0; i < EnumGemOre.VALUES.length; i++) {
			EnumGemOre gem = EnumGemOre.VALUES[i];
			OreDictionary.registerOre("gem"+gem.name().charAt(0)+gem.getName().substring(1), new ItemStack(GEM, 1, i));
		}
	}
	
	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> e) {
		GLASS_HEART = new BlockGlassHeart();
		GLASS_HEART.setRegistryName("glass_heart");
		GLASS_HEART.setCreativeTab(CREATIVE_TAB);
		GLASS_HEART.setUnlocalizedName("glasshearts.glass_heart");
		e.getRegistry().register(GLASS_HEART);
		
		ORE = new BlockOre();
		ORE.setRegistryName("ore");
		ORE.setCreativeTab(CREATIVE_TAB);
		ORE.setUnlocalizedName("glasshearts.ore");
		e.getRegistry().register(ORE);
		
		PETRIFIED_LOG = new BlockPetrifiedLog();
		PETRIFIED_LOG.setRegistryName("petrified_log");
		PETRIFIED_LOG.setCreativeTab(CREATIVE_TAB);
		PETRIFIED_LOG.setUnlocalizedName("glasshearts.petrified_log");
		e.getRegistry().register(PETRIFIED_LOG);
		
		LIFEFORCE_BLOCK = new BlockFluidLifeforce(LIFEFORCE, Material.WATER);
		LIFEFORCE.setBlock(LIFEFORCE_BLOCK);
		
		LIFEFORCE_BLOCK.setRegistryName("lifeforce_block");
		e.getRegistry().register(LIFEFORCE_BLOCK);
	}
	
	@SubscribeEvent
	public void onRegisterSounds(RegistryEvent.Register<SoundEvent> e) {
		SAP = new SoundEvent(new ResourceLocation("glasshearts", "sap"));
		SAP.setRegistryName("sap");
		e.getRegistry().register(SAP);
		
		ATTUNE = new SoundEvent(new ResourceLocation("glasshearts", "attune"));
		ATTUNE.setRegistryName("attune");
		e.getRegistry().register(ATTUNE);
	}
	
	@SubscribeEvent
	public void onRegisterRecipes(RegistryEvent.Register<IRecipe> e) {
		e.getRegistry().register(new ShapedOreRecipe(null, STAFF,
				"  o",
				" //",
				"/  ",
				
				'/', Items.BLAZE_ROD,
				'o', "gemOpal").setRegistryName("opal_staff"));
		
		e.getRegistry().register(new ShapedOreRecipe(null, new ItemStack(GLASS_HEART, 1, 0),
				"g g",
				"ggg",
				" g ",
				
				'g', "blockGlassColorless").setRegistryName("glass_heart"));
		
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
			e.getRegistry().register(new ShapedOreRecipe(null, new ItemStack(GLASS_HEART, 1, 16-i),
					"g g",
					"ggg",
					" g ",
					
					'g', "blockGlass"+dyes[i]).setRegistryName(dyes[i].toLowerCase(Locale.ROOT)+"_glass_heart"));
		}
	}
	
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.onPostInit();
	}
	
	@SubscribeEvent
	public void onNewRegistry(RegistryEvent.NewRegistry e) {
		Gem.registerGemRegistry();
	}
	
	@SubscribeEvent
	public void onPopulate(ChunkGeneratorEvent.ReplaceBiomeBlocks e) {
		if (e.getWorld().provider.isSurfaceWorld() && configGeneratePetrifiedTrees) {
			// This is DEFINITELY not a hack. Nope. Not at all.
			GeneratePetrifiedTree.generate(e.getWorld().getSeed()+(e.getX()^e.getZ()), e.getPrimer(), e.getGen());
		}
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
		if (e.getEntityLiving().hasCapability(CapabilityHeartHandler.CAPABILITY, null)) {
			IHeartHandler cap = e.getEntityLiving().getCapability(CapabilityHeartHandler.CAPABILITY, null);
			amt = (Float)applyArmorCalculations.invoke(e.getEntityLiving(), e.getSource(), amt);
			amt = (Float)applyPotionDamageCalculations.invoke(e.getEntityLiving(), e.getSource(), amt);

			if (amt != 0) {
				e.getEntityLiving().getCombatTracker().trackDamage(e.getSource(), cap.totalHealth()*2, amt);
				float absorb = Math.min(amt, e.getEntityLiving().getAbsorptionAmount());
				e.getEntityLiving().setAbsorptionAmount(e.getEntityLiving().getAbsorptionAmount() - absorb);
				amt -= absorb;
				cap.damage(amt/2, e.getSource());
			}
			e.getEntityLiving().setHealth((int)(cap.totalHealth()*2));
			e.setAmount(0);
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onHeal(LivingHealEvent e) {
		if (e.getEntityLiving().hasCapability(CapabilityHeartHandler.CAPABILITY, null)) {
			IHeartHandler cap = e.getEntityLiving().getCapability(CapabilityHeartHandler.CAPABILITY, null);
			float amt = e.getAmount()/2f;
			float taken = cap.heal(amt);
			if (taken < amt) {
				float waste = (amt-taken);
				if (waste >= 0.5f && e.getEntityLiving() instanceof EntityPlayer) {
					// I like this effect, but it triggers randomly due to hunger and such
					//new PlayHeartEffectMessage(PlayHeartEffectMessage.EFFECT_WASTED_HEALING, (int)(waste*2)-1, cap.getContainers()-1).sendTo((EntityPlayer)e.getEntityLiving());
				}
			}
			e.getEntityLiving().setHealth((int)(cap.totalHealth()*2));
			e.setAmount(0);
		}
	}
	
	@SubscribeEvent
	public void onExplode(ExplosionEvent.Detonate e) {
		if (e.getExplosion().getExplosivePlacedBy() instanceof EntityCreeper) {
			EntityCreeper ec = ((EntityCreeper)e.getExplosion().getExplosivePlacedBy());
			if (ec.getTags().contains("glasshearts:found_heart")) {
				if (configCreeperFakeExplosions || !e.getWorld().getGameRules().getBoolean("mobGriefing")) {
					Iterator<BlockPos> iter = e.getAffectedBlocks().iterator();
					while (iter.hasNext()) {
						BlockPos pos = iter.next();
						if (e.getWorld().getBlockState(pos).getBlock() != GLASS_HEART) {
							iter.remove();
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onExplosionStart(ExplosionEvent.Start e) {
		if (e.getExplosion().getExplosivePlacedBy() instanceof EntityCreeper) {
			EntityCreeper ec = ((EntityCreeper)e.getExplosion().getExplosivePlacedBy());
			if (ec.getTags().contains("glasshearts:found_heart")) {
				if (!e.getWorld().getGameRules().getBoolean("mobGriefing")) {
					e.setCanceled(true);
					boolean oldFakeExplosions = configCreeperFakeExplosions;
					try {
						e.getWorld().getGameRules().setOrCreateGameRule("mobGriefing", "true");
						configCreeperFakeExplosions = true;
						explode.invoke(ec);
					} finally {
						e.getWorld().getGameRules().setOrCreateGameRule("mobGriefing", "false");
						configCreeperFakeExplosions = oldFakeExplosions;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerSave(PlayerEvent.SaveToFile e) {
		if (e.getEntityPlayer().hasCapability(CapabilityHeartHandler.CAPABILITY, null)) {
			IHeartHandler cap = e.getEntityPlayer().getCapability(CapabilityHeartHandler.CAPABILITY, null);
			
			File f;
			if (e.getEntityPlayer().getName().equals(e.getEntityPlayer().getServer().getServerOwner())) {
				// singleplayer
				f = new File(e.getEntityPlayer().world.getSaveHandler().getWorldDirectory(), "glasshearts_sp.dat");
			} else {
				// multiplayer
				f = e.getPlayerFile("glasshearts.dat");
			}
			
			NBTBase nbt = CapabilityHeartHandler.CAPABILITY.writeNBT(cap, null);
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("Version", 1);
			tag.setTag("Hearts", nbt);
			
			try {
				CompressedStreamTools.safeWrite(tag, f);
			} catch (IOException ex) {
				LOG.error("Error while saving player heart data for {}", e.getEntityPlayer().getName(), ex);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerLoad(PlayerEvent.LoadFromFile e) {
		File f;
		if (e.getEntityPlayer().getName().equals(e.getEntityPlayer().getServer().getServerOwner())) {
			// singleplayer
			f = new File(e.getEntityPlayer().world.getSaveHandler().getWorldDirectory(), "glasshearts_sp.dat");
			if (!f.exists() && e.getPlayerFile("glasshearts.dat").exists()) {
				// compatibility with old worlds
				f = e.getPlayerFile("glasshearts.dat");
			}
		} else {
			// multiplayer
			f = e.getPlayerFile("glasshearts.dat");
		}
		
		if (f.exists()) {
			if (e.getEntityPlayer().hasCapability(CapabilityHeartHandler.CAPABILITY, null)) {
				IHeartHandler cap = e.getEntityPlayer().getCapability(CapabilityHeartHandler.CAPABILITY, null);
				
				try {
					NBTTagCompound tag = CompressedStreamTools.read(f);
					int version = tag.getInteger("Version");
					if (version == 0) {
						LogManager.getLogger("GlassHearts").warn("Ignoring old v0 Glass Hearts data due to the ghost hearts fix");
						return;
					}
					NBTBase nbt = tag.getTag("Hearts");
					CapabilityHeartHandler.CAPABILITY.readNBT(cap, null, nbt);
				} catch (IOException ex) {
					LOG.error("Error while loading player heart data for {}", e.getEntityPlayer().getName(), ex);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerAttachCapabilities(AttachCapabilitiesEvent<Entity> e) {
		if (e.getObject() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)e.getObject();
			EntityHeartHandler bhh = new EntityHeartHandler(player);
			e.addCapability(new ResourceLocation("glasshearts", "health"), new ICapabilityProvider() {
				
				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return facing == null && capability == CapabilityHeartHandler.CAPABILITY;
				}
				
				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					if (capability == CapabilityHeartHandler.CAPABILITY && facing == null) {
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
				if (e.world.isBlockLoaded(ghd.getHeartPos())) {
					TileEntity te = e.world.getTileEntity(ghd.getHeartPos());
					if (!(te instanceof TileEntityGlassHeart)) {
						LOG.warn("Deleting orphaned Glass Heart at {}, {}, {}", ghd.getHeartPos().getX(), ghd.getHeartPos().getY(), ghd.getHeartPos().getZ());
						if (remove.isEmpty()) {
							remove = Sets.newHashSet();
						}
						remove.add(ghd.getHeartPos());
					} else {
						update((TileEntityGlassHeart)te, e.world.getTotalWorldTime());
					}
				} else {
					update(ghd, e.world.getTotalWorldTime());
				}
			}
			for (BlockPos pos : remove) {
				data.remove(pos);
			}
		}
	}
	
	public void update(IGlassHeart igh, long ticks) {
		if (igh.getGem() != Gems.NONE && igh.getGem().getState(igh) != EnumGemState.INACTIVE) {
			Gem originalGem = igh.getGem();
			igh.getGem().update(igh, ticks);
			if (igh.getLifeforce() == 0 && igh.hasBeenFull()) {
				igh.getGem().onEmpty(igh);
			}
			if (igh.getGem() == Gems.NONE) {
				if (igh.getHeartWorld() instanceof WorldServer) {
					((WorldServer)igh.getHeartWorld()).spawnParticle(EnumParticleTypes.ITEM_CRACK,
							igh.getHeartPos().getX()+0.5, igh.getHeartPos().getY()+0.5, igh.getHeartPos().getZ()+0.5, 32,
							0, 0, 0, 0.2, Item.getIdFromItem(igh.getGemStack().getItem()), igh.getGemStack().getMetadata());
					igh.getHeartWorld().playSound(null, igh.getHeartPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1f, 2f);
				}
				for (EntityPlayer ep : getAllOnlineAttunedPlayers(igh)) {
					IHeartHandler ihh = ep.getCapability(CapabilityHeartHandler.CAPABILITY, null);
					for (int i = 0; i < ihh.getContainers(); i++) {
						HeartContainer hc = ihh.getContainer(i);
						HeartContainerOwner owner = hc.getOwner();
						if (owner != null && owner instanceof BlockHeartContainerOwner) {
							BlockHeartContainerOwner bhco = (BlockHeartContainerOwner)owner;
							if (Objects.equal(bhco.getPos(), igh.getHeartPos())) {
								hc = hc.copy();
								hc.setGem(Gems.NONE);
								ihh.setContainer(i, hc);
								new PlayHeartEffectMessage(PlayHeartEffectMessage.EFFECT_GEM_SHATTER, Gem.getIdForGem(originalGem), i).sendTo(ep);
							}
						}
					}
				}
			}
		}
		if (igh.getHeartWorld() == null || !igh.getHeartWorld().isRemote) {
			if (igh.getLifeforce() == 0) {
				igh.setHasBeenFull(false);
			}
			if (igh.getLifeforce() == igh.getLifeforceCapacity()) {
				igh.setHasBeenFull(true);
			}
		}
		
		if (igh.getLifeforceBuffer() > 0 && igh.getLifeforce() < igh.getLifeforceCapacity()) {
			int rate = igh.getGem().adjustFillRate(configGlassHeartFillRate);
			int amt = Math.min(rate, Math.min(igh.getLifeforceBuffer(), igh.getLifeforceCapacity()-igh.getLifeforce()));
			igh.setLifeforceBuffer(igh.getLifeforceBuffer()-amt);
			igh.setLifeforce(igh.getLifeforce()+amt);
		}
	}
	
	@SubscribeEvent
	public void onJoinWorld(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof EntityCreeper) {
			if (configCreepersSeekHearts) {
				EntityCreeper creeper = ((EntityCreeper)e.getEntity());
				creeper.tasks.addTask(3, new EntityAICreeperSeekHeart(creeper, 1));
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		if (!e.getEntityLiving().world.isRemote) {
			DamageSource src = e.getSource();
			if (src.getTrueSource() instanceof EntityPlayer) {
				EntityPlayer p = (EntityPlayer)src.getTrueSource();
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
							new ParticleEffectMessage(el.posX, el.posY, el.posZ, p, ParticleEffectMessage.EFFECT_SAP).sendToAllWatching(el);
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
							if (!player.isCreative()) {
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
	}
	
	public static List<EntityPlayer> getAllOnlineAttunedPlayers(IGlassHeart igh) {
		Iterable<? extends EntityPlayer> players;
		if (igh.getHeartWorld() != null) {
			if (igh.getHeartWorld().getMinecraftServer() != null) {
				players = igh.getHeartWorld().getMinecraftServer().getPlayerList().getPlayers();
			} else {
				players = igh.getHeartWorld().playerEntities;
			}
		} else {
			return Collections.emptyList();
		}
		List<EntityPlayer> li = Lists.newArrayList();
		for (EntityPlayer ep : players) {
			if (ep.hasCapability(CapabilityHeartHandler.CAPABILITY, null)) {
				IHeartHandler ihh = ep.getCapability(CapabilityHeartHandler.CAPABILITY, null);
				for (HeartContainer hc : ihh) {
					HeartContainerOwner owner = hc.getOwner();
					if (owner instanceof BlockHeartContainerOwner) {
						BlockHeartContainerOwner bhco = (BlockHeartContainerOwner)owner;
						if (Objects.equal(bhco.getPos(), igh.getHeartPos())) {
							li.add(ep);
							break;
						}
					}
				}
			}
		}
		return li;
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
			if (ws.getPlayerChunkMap().isPlayerWatchingChunk(player, c.x, c.z)) {
				player.connection.sendPacket(packet);
			}
		}
	}
	
}
