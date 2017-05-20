package com.elytradev.glasshearts.block;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.enums.EnumGem;
import com.elytradev.glasshearts.item.ItemGem;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockOre extends Block {

	public static final EnumGem[] VALID_GEMS = {
		EnumGem.AMETHYST,
		EnumGem.RUBY,
		EnumGem.TOPAZ,
		EnumGem.SAPPHIRE,
		EnumGem.OPAL,
		EnumGem.ONYX,
		EnumGem.AGATE,
	};
	
	public static final PropertyEnum<EnumGem> VARIANT = PropertyEnum.create("variant", EnumGem.class, VALID_GEMS);
	
	public BlockOre() {
		super(Material.ROCK);
		setHardness(3);
		setResistance(5);
		setSoundType(SoundType.STONE);
		setHarvestLevel("pickaxe", 2);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return ArrayUtils.indexOf(VALID_GEMS, state.getValue(VARIANT));
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(VARIANT, VALID_GEMS[meta%VALID_GEMS.length]);
	}
	
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < VALID_GEMS.length; i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}
	
	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(this, 1, ArrayUtils.indexOf(ItemGem.VALID_GEMS, state.getValue(VARIANT)));
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return getSilkTouchDrop(state);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return GlassHearts.inst.GEM;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return ArrayUtils.indexOf(ItemGem.VALID_GEMS, state.getValue(VARIANT));
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 1;
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		int i = 0;
		if (fortune > 0) {
			i = random.nextInt(fortune + 2) - 1;
			
			if (i < 0) {
				i = 0;
			}
		}

		return quantityDropped(random) * (i + 1);
	}
	
}
