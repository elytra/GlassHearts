package com.elytradev.glasshearts.enums;

import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.item.ItemGem;
import com.elytradev.glasshearts.logic.HeartContainer;
import com.elytradev.glasshearts.logic.IGlassHeart;
import com.elytradev.glasshearts.network.ParticleEffectMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;

public enum EnumGem implements IStringSerializable {
	NONE(0) {
		@Override
		public ItemStack toItemStack() {
			return ItemStack.EMPTY;
		}
	},
	
	/**
	 * Magic damage to this heart is 80% less effective.
	 */
	EMERALD(0x56BB73) {
		@Override
		public ItemStack toItemStack() {
			return new ItemStack(Items.EMERALD);
		}
		@Override
		public float getMultiplier(DamageSource src) {
			if ("magic".equals(src.damageType)) {
				return 0.2f;
			}
			return 1;
		}
	},
	/**
	 * Gives you Regeneration when the container is emptied. Note that
	 * Regeneration does not work on glass hearts.
	 */
	AMETHYST(0x5A399B) {
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.hasBeenFull() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
		@Override
		public void onEmpty(IGlassHeart igh) {
			for (EntityPlayer ep : GlassHearts.getAllOnlineAttunedPlayers(igh)) {
				new ParticleEffectMessage(ep.posX, ep.posY, ep.posZ, ep, ParticleEffectMessage.EFFECT_AMETHYST).sendToAllWatching(ep);
				// 10 seconds of Regen II
				ep.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 1));
			}
		}
	},
	/**
	 * Container immediately refills after being emptied, but the gem shatters.
	 */
	RUBY(0x874446) {
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.hasBeenFull() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
		@Override
		public void onEmpty(IGlassHeart igh) {
			igh.setGem(EnumGem.NONE);
			// :^)
			igh.setLifeforce(igh.getLifeforceCapacity()-1);
		}
	},
	/**
	 * The lifeforce buffer drains into the main buffer twice as fast.
	 */
	DIAMOND(0x81C2B5) {
		@Override
		public ItemStack toItemStack() {
			return new ItemStack(Items.DIAMOND);
		}
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.getLifeforceBuffer() > 0 ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
		@Override
		public int adjustFillRate(int fillRate) {
			return fillRate+(fillRate/2);
		}
	},
	/**
	 * Gives you Absorption when the container is emptied.
	 */
	TOPAZ(0xB27639) {
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.hasBeenFull() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
		@Override
		public void onEmpty(IGlassHeart igh) {
			for (EntityPlayer ep : GlassHearts.getAllOnlineAttunedPlayers(igh)) {
				new ParticleEffectMessage(ep.posX, ep.posY, ep.posZ, ep, ParticleEffectMessage.EFFECT_TOPAZ).sendToAllWatching(ep);
				// 30 seconds of Absorption
				ep.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 600, 0));
			}
		}
	},
	/**
	 * If this container is at least half full, damage dealt will be capped to
	 * this container only.
	 */
	SAPPHIRE(0x3A4780) {
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.getLifeforce() > (igh.getLifeforceCapacity()/2) ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
		@Override
		public boolean doesBlockDamage(DamageSource src, HeartContainer hc) {
			return hc.getFillAmount() >= 0.5f;
		}
	},
	/**
	 * Container very slowly refills.
	 */
	OPAL(0x94B1AC) {
		@Override
		public EnumGemState getState(IGlassHeart igh) {
			return igh.getLifeforceBuffer() <= 0 && igh.getLifeforce() < igh.getLifeforceCapacity() ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
		}
		@Override
		public void update(IGlassHeart igh, long ticks) {
			if (ticks%10 == 0) {
				if (igh.getLifeforce() < igh.getLifeforceCapacity()) {
					igh.setLifeforce(igh.getLifeforce()+1);
				}
			}
		}
	},
	/**
	 * Wither damage to this heart is 80% less effective.
	 */
	ONYX(0x434343) {
		@Override
		public float getMultiplier(DamageSource src) {
			if ("wither".equals(src.damageType)) {
				return 0.2f;
			}
			return 1;
		}
	},
	/**
	 * All damage to this heart is 50% less effective.
	 */
	AMBER(0xC2983A) {
		@Override
		public float getMultiplier(DamageSource src) {
			return 0.5f;
		}
	},
	;
	
	private final String name;
	public final String oreDictionary;
	public final int color;
	
	private ItemStack renderingSingleton;
	
	private EnumGem(int color) {
		this.color = color;
		this.name = name().toLowerCase(Locale.ROOT);
		oreDictionary = "gem"+Character.toString(name().charAt(0))+this.name.substring(1);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public ItemStack getRenderingSingleton() {
		if (renderingSingleton == null) {
			renderingSingleton = toItemStack();
		}
		return renderingSingleton;
	}
	
	public EnumGemState getState(IGlassHeart igh) {
		return igh.getLifeforce() > 0 ? EnumGemState.ACTIVE_BENEFICIAL : EnumGemState.INACTIVE;
	}
	
	public ItemStack toItemStack() {
		int idx = ArrayUtils.indexOf(ItemGem.VALID_GEMS, this);
		if (idx == -1) throw new AssertionError("toItemStack not overridden for special gem "+this);
		return new ItemStack(GlassHearts.inst.GEM, 1, idx);
	}
	
	public static EnumGem fromItemStack(ItemStack stack) {
		if (stack.getItem() == Items.DIAMOND) {
			return DIAMOND;
		}
		if (stack.getItem() == Items.EMERALD) {
			return EMERALD;
		}
		if (stack.getItem() == GlassHearts.inst.GEM) {
			return ItemGem.VALID_GEMS[stack.getMetadata()%ItemGem.VALID_GEMS.length];
		}
		return null;
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
}
