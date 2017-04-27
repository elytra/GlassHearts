package com.elytradev.glasshearts.world;

import java.util.Random;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.block.BlockPetrifiedLog;

import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class GeneratePetrifiedTree {

	private static final Accessor<Biome[]> biomesForGeneration = Accessors.findField(ChunkProviderOverworld.class, "field_185981_C", "biomesForGeneration", "D");
	
	private static Random random = new Random(0);
	
	public static void generate(long seed, ChunkPrimer primer, IChunkGenerator gen) {
		if (!(gen instanceof ChunkProviderOverworld)) return;
		
		random.setSeed(seed);
		
		int x = 8;
		int z = 8;
		int y = 128;
		
		Biome[] biomes = biomesForGeneration.get(gen);
		
		Biome b = biomes[z + (x*16)];
		if (b != null && BiomeDictionary.hasType(b, Type.FOREST)) {
			if (random.nextInt(5) == 0) {
				while (true) {
					if (y <= 0) return;
					IBlockState ibs = primer.getBlockState(x, y, z);
					if (ibs.getBlock() != Blocks.AIR) {
						y++;
						break;
					}
					y--;
				}
				IBlockState brownMush = Blocks.BROWN_MUSHROOM.getDefaultState();
				IBlockState redMush = Blocks.RED_MUSHROOM.getDefaultState();
				EnumAxis axis = EnumAxis.values()[random.nextInt(3)];
				int length = random.nextInt(3)+4;
				IBlockState ibs = GlassHearts.inst.PETRIFIED_LOG.getDefaultState().withProperty(BlockPetrifiedLog.LOG_AXIS, axis);
				switch (axis) {
					case X: {
						x -= length/2;
						for (int i = 0; i < length; i++) {
							primer.setBlockState(x+i, y, z, ibs);
							if (random.nextInt(3) == 0) {
								primer.setBlockState(x+i, y+1, z, random.nextBoolean() ? brownMush : redMush);
							}
						}
						break;
					}
					case Z: {
						z -= length/2;
						for (int i = 0; i < length; i++) {
							primer.setBlockState(x, y, z+i, ibs);
							if (random.nextInt(3) == 0) {
								primer.setBlockState(x, y+1, z+i, random.nextBoolean() ? brownMush : redMush);
							}
						}
						break;
					}
					case Y: {
						int up = random.nextInt(3)+1;
						length -= up;
						for (int i = 0; i < up; i++) {
							primer.setBlockState(x, y+i, z, ibs);
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
									primer.setBlockState(x+i, y, z, ibs2);
									if (random.nextInt(3) == 0) {
										primer.setBlockState(x+i, y+1, z, random.nextBoolean() ? brownMush : redMush);
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
									primer.setBlockState(x, y, z+i, ibs2);
									if (random.nextInt(3) == 0) {
										primer.setBlockState(x, y+1, z+i, random.nextBoolean() ? brownMush : redMush);
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
