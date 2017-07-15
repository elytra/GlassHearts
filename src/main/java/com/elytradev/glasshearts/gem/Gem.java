package com.elytradev.glasshearts.gem;

import org.apache.commons.lang3.ArrayUtils;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.client.HeartRenderer;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.init.Gems;
import com.elytradev.glasshearts.logic.HeartContainer;
import com.elytradev.glasshearts.logic.IGlassHeart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.IForgeRegistryEntry;;

public abstract class Gem extends IForgeRegistryEntry.Impl<Gem> {
	public static ForgeRegistry<Gem> REGISTRY;
	private static final ResourceLocation MISSING = new ResourceLocation("glasshearts", "this/definitely/does/not/exist.png");
	
	public static Gem getGemById(int id) {
		return REGISTRY.getValue(id);
	}
	
	public static int getIdForGem(Gem gem) {
		return REGISTRY.getID(gem);
	}
	
	
	private final String oreName;
	private final int color;
	
	private ResourceLocation tex = MISSING;
	private int texWidth = 256;
	private int texHeight = 256;
	private int u = 0;
	private int v = 0;
	private int width = 256;
	private int height = 256;
	
	public Gem(int color, String oreName) {
		this.color = color;
		this.oreName = oreName;
	}
	
	protected Gem setDefaultTexture(int u) {
		return setTexture(GlassHearts.TEX, HeartRenderer.TEXTURE_WIDTH, HeartRenderer.TEXTURE_HEIGHT, 9+(u*9), 54, 9, 9);
	}
	
	protected Gem setTexture(ResourceLocation tex) {
		return setTexture(tex, 9, 9, 0, 0, 9, 9);
	}
	
	protected Gem setTexture(ResourceLocation tex, int u, int v, int width, int height) {
		return setTexture(tex, 256, 256, u, v, width, height);
	}
	
	protected Gem setTexture(ResourceLocation tex, int texWidth, int texHeight, int u, int v, int width, int height) {
		this.tex = tex;
		this.texWidth = texWidth;
		this.texHeight = texHeight;
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
		return this;
	}
	
	public int getColor() {
		return color;
	}
	
	public EnumGemState getState(IGlassHeart igh) {
		return igh.getLifeforce() > 0 ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	public static Gem fromItemStack(ItemStack stack) {
		if (stack.isEmpty()) return Gems.NONE;
		for (Gem gem : REGISTRY.getValues()) {
			if (gem.itemStackMatches(stack)) {
				return gem;
			}
		}
		return Gems.NONE;
	}
	
	public boolean itemStackMatches(ItemStack stack) {
		if (oreName == null) return false;
		int oreId = OreDictionary.getOreID(oreName);
		return ArrayUtils.contains(OreDictionary.getOreIDs(stack), oreId);
	}

	public float getMultiplier(DamageSource src) {
		return 1;
	}
	
	public void onEmpty(IGlassHeart igh) {
		
	}

	public void update(IGlassHeart igh, long ticks) {
		
	}
	
	public boolean doesBlockDamage(DamageSource src, HeartContainer hc) {
		return false;
	}

	public int adjustFillRate(int fillRate) {
		return fillRate;
	}
	
	public ResourceLocation getTexture() {
		return tex;
	}
	
	public float getMinU() {
		return (u)/(float)texWidth;
	}
	public float getMaxU() {
		return (u+width)/(float)texWidth;
	}
	public float getMinV() {
		return (v)/(float)texHeight;
	}
	public float getMaxV() {
		return (v+height)/(float)texHeight;
	}
	
	
	public int getTextureWidth() {
		return texWidth;
	}
	public int getTextureHeight() {
		return texHeight;
	}
	public int getU() {
		return u;
	}
	public int getV() {
		return v;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}

	
	public static void registerGemRegistry() {
		REGISTRY = (ForgeRegistry<Gem>)new RegistryBuilder<Gem>()
				.setIDRange(0, 254)
				.setName(new ResourceLocation("glasshearts", "gems"))
				.setType(Gem.class)
				.setDefaultKey(new ResourceLocation("glasshearts", "none"))
				.create();
	}
	
	public static void registerGems() {
		REGISTRY.registerAll(
			(Gems.NONE = new GemNone()).setRegistryName("none"),
			
			(Gems.AGATE = new GemAgate()).setRegistryName("agate"),
			(Gems.AMBER = new GemAmber()).setRegistryName("amber"),
			(Gems.AMETHYST = new GemAmethyst()).setRegistryName("amethyst"),
			(Gems.DIAMOND = new GemDiamond()).setRegistryName("diamond"),
			(Gems.EMERALD = new GemEmerald()).setRegistryName("emerald"),
			(Gems.ONYX = new GemOnyx()).setRegistryName("onyx"),
			(Gems.OPAL = new GemOpal()).setRegistryName("opal"),
			(Gems.RUBY = new GemRuby()).setRegistryName("ruby"),
			(Gems.SAPPHIRE = new GemSapphire()).setRegistryName("sapphire"),
			(Gems.TOPAZ = new GemTopaz()).setRegistryName("topaz"));
	}
}
