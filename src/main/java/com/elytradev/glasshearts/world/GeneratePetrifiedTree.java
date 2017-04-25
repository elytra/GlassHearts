package com.elytradev.glasshearts.world;

import java.util.Random;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.block.BlockPetrifiedLog;

import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GeneratePetrifiedTree implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (!world.provider.isSurfaceWorld()) return;
		// This is the OPPOSITE of recommended advice for populators.
		
		// But, the generator below should never leave chunk bounds, and this
		// lets us dodge already-generated trees.
		int x = (chunkX*16)+16;
		int z = (chunkZ*16)+16;
		int y = world.getHeight(x, z);
		Biome b = world.getBiome(new BlockPos(x, y, z));
		if (b != null && BiomeDictionary.areSimilar(Biomes.FOREST, b)) {
			if (random.nextInt(5) == 0) {
				IBlockState brownMush = Blocks.BROWN_MUSHROOM.getDefaultState();
				IBlockState redMush = Blocks.RED_MUSHROOM.getDefaultState();
				EnumAxis axis = EnumAxis.values()[random.nextInt(3)];
				int length = random.nextInt(3)+4;
				IBlockState ibs = GlassHearts.inst.PETRIFIED_LOG.getDefaultState().withProperty(BlockPetrifiedLog.LOG_AXIS, axis);
				switch (axis) {
					case X: {
						x -= length/2;
						for (int i = 0; i < length; i++) {
							world.setBlockState(new BlockPos(x+i, y, z), ibs);
							if (random.nextInt(3) == 0) {
								world.setBlockState(new BlockPos(x+i, y+1, z), random.nextBoolean() ? brownMush : redMush);
							}
						}
						break;
					}
					case Z: {
						z -= length/2;
						for (int i = 0; i < length; i++) {
							world.setBlockState(new BlockPos(x, y, z+i), ibs);
							if (random.nextInt(3) == 0) {
								world.setBlockState(new BlockPos(x, y+1, z+i), random.nextBoolean() ? brownMush : redMush);
							}
						}
						break;
					}
					case Y: {
						int up = random.nextInt(3)+1;
						length -= up;
						for (int i = 0; i < up; i++) {
							world.setBlockState(new BlockPos(x, y+i, z), ibs);
						}
						EnumAxis axis2 = random.nextBoolean() ? EnumAxis.X : EnumAxis.Z;
						IBlockState ibs2 = GlassHearts.inst.PETRIFIED_LOG.getDefaultState().withProperty(BlockPetrifiedLog.LOG_AXIS, axis2);
						boolean negative = random.nextBoolean();
						switch (axis2) {
							case X:
								if (negative) {
									x -= length+1;
								} else {
									x += 2;
								}
								for (int i = 0; i < length; i++) {
									world.setBlockState(new BlockPos(x+i, y, z), ibs2);
									if (random.nextInt(3) == 0) {
										world.setBlockState(new BlockPos(x+i, y+1, z), random.nextBoolean() ? brownMush : redMush);
									}
								}
								break;
							case Z:
								if (negative) {
									z -= length+1;
								} else {
									z += 2;
								}
								for (int i = 0; i < length; i++) {
									world.setBlockState(new BlockPos(x, y, z+i), ibs2);
									if (random.nextInt(3) == 0) {
										world.setBlockState(new BlockPos(x, y+1, z+i), random.nextBoolean() ? brownMush : redMush);
									}
								}
								break;
							default: break;
						}
					}
					default: break;
				}
			}
		}
	}

}
