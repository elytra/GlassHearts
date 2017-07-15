package com.elytradev.glasshearts.entity;

import com.elytradev.glasshearts.GlassHearts;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityAICreeperSeekHeart extends EntityAIMoveToBlock {
	private final EntityCreeper creeper;

	public EntityAICreeperSeekHeart(EntityCreeper creeper, double speedIn) {
		super(creeper, speedIn, 16);
		this.creeper = creeper;
	}

	@Override
	public boolean shouldExecute() {
		if (runDelay <= 0) {
			if (!GlassHearts.inst.configCreepersSeekHearts) {
				return false;
			}
		}
		return super.shouldExecute();
	}

	@Override
	public void updateTask() {
		super.updateTask();
		creeper.getLookHelper().setLookPosition(destinationBlock.getX() + 0.5, destinationBlock.getY() + 1, destinationBlock.getZ() + 0.5, 10f, creeper.getVerticalFaceSpeed());

		if (creeper.getDistanceSqToCenter(destinationBlock) < 4) {
			creeper.ignite();
			creeper.addTag("glasshearts:found_heart");
		}
	}

	@Override
	protected boolean shouldMoveTo(World world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		if (block == GlassHearts.inst.GLASS_HEART) {
			return true;
		}
		return false;
	}
}
