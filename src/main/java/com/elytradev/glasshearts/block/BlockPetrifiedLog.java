package com.elytradev.glasshearts.block;

import java.util.List;

import com.elytradev.glasshearts.GlassHearts;
import com.google.common.collect.Lists;

import net.minecraft.block.BlockLog;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockPetrifiedLog extends BlockLog {

	public BlockPetrifiedLog() {
		setDefaultState(blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, LOG_AXIS);
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(GlassHearts.inst.PETRIFIED_LOG, 1, 0);
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> li = Lists.newArrayList();
		for (int i = 0; i < 4+(RANDOM.nextInt(fortune+1)); i++) {
			li.add(new ItemStack(Items.STICK));
		}
		if (RANDOM.nextInt(8-fortune) == 0) {
			li.add(new ItemStack(GlassHearts.inst.GEM, 1, 6));
		}
		return li;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState();

		switch (meta & 0xC) {
			case 0:
				state = state.withProperty(LOG_AXIS, EnumAxis.Y);
				break;
			case 4:
				state = state.withProperty(LOG_AXIS, EnumAxis.X);
				break;
			case 8:
				state = state.withProperty(LOG_AXIS, EnumAxis.Z);
				break;
			default:
				state = state.withProperty(LOG_AXIS, EnumAxis.NONE);
				break;
		}

		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = 0;

		switch (state.getValue(LOG_AXIS)) {
			case X:
				i |= 4;
				break;
			case Z:
				i |= 8;
				break;
			case NONE:
				i |= 12;
				break;
			default:
				break;
		}

		return i;
	}
	
}
