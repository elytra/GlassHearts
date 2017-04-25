package com.elytradev.glasshearts.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockFluidLifeforce extends BlockFluidClassic {

	public BlockFluidLifeforce(Fluid fluid, Material material) {
		super(fluid, material);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		if (!worldIn.isRemote) {
			if (entityIn instanceof EntityLivingBase) {
				if (((EntityLivingBase)entityIn).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
					entityIn.attackEntityFrom(DamageSource.MAGIC, 1f);
				}
			}
		}
	}

}
