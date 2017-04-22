package com.elytradev.glasshearts.world;

import java.util.Random;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.block.BlockOre;
import com.elytradev.glasshearts.enums.EnumGem;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GenerateGems implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		for (int i = 0; i < 12; i++) {
			int x = (chunkX*16)+random.nextInt(16);
			int y = random.nextInt(24)+3;
			int z = (chunkZ*16)+random.nextInt(16);
			EnumGem gem = BlockOre.VALID_GEMS[random.nextInt(BlockOre.VALID_GEMS.length)];
			world.setBlockState(new BlockPos(x, y, z), GlassHearts.inst.ORE.getDefaultState().withProperty(BlockOre.VARIANT, gem));
		}
	}

}
