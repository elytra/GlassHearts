package com.elytradev.glasshearts.integration.tcon;

import java.util.List;

import com.elytradev.glasshearts.GlassHearts;
import com.google.common.collect.ImmutableList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolBuilder;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

public class ModifierSapping extends ToolModifier {

	// I don't know what I'm doing
	// What even is the Tinkers' API
	
	public ModifierSapping() {
		super("glasshearts.tcon.sapping", 0xFF1313);
		
		addAspects(ModifierAspect.weaponOnly);
		addAspects(new ModifierAspect.MultiAspect(this, 10, 3, 1));
	}

	@Override
	public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
		ModifierNBT data = ModifierNBT.readTag(modifierTag);
		for (int i = 0; i < data.level; i++) {
			ToolBuilder.addEnchantment(rootCompound, GlassHearts.inst.SAPPING);
		}
	}
	
	@Override
	public List<String> getExtraInfo(ItemStack tool, NBTTagCompound modifierTag) {
		String loc = String.format(LOC_Extra, getIdentifier());
		return ImmutableList.of(
					Util.translateFormatted(loc, Util.dfPercent.format(EnchantmentHelper.getEnchantmentLevel(GlassHearts.inst.SAPPING, tool)/10f))
				);
	}
	
	@Override
	public String getTooltip(NBTTagCompound modifierTag, boolean detailed) {
		ModifierNBT data = ModifierNBT.readTag(modifierTag);
		int level = data.level;

		String tooltip = getLocalizedName();
		if (level > 0) {
			tooltip += " " + TinkerUtil.getRomanNumeral(level);
		}

		if (detailed) {
			tooltip += " " + data.extraInfo;
		}
		return tooltip;
	}

}
