package com.elytradev.glasshearts.block;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;

import com.elytradev.glasshearts.EnumGem;
import com.elytradev.glasshearts.EnumGlassColor;
import com.elytradev.glasshearts.GlassHeartWorldData;
import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.item.ItemGem;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class BlockGlassHeart extends Block {
	
	public static final PropertyBool INNER = PropertyBool.create("inner");
	public static final PropertyBool STAINED = PropertyBool.create("stained");
	
	public BlockGlassHeart() {
		super(Material.GLASS);
		setHardness(0.5f);
		setResistance(0.3f);
		setSoundType(SoundType.GLASS);
		setLightOpacity(0);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, INNER, STAINED);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			if (tegh.getLifeforceBuffer() < GlassHearts.inst.configGlassHeartCapacity) {
				try {
					if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
						IFluidHandlerItem ifhi = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
						FluidStack rtrn = ifhi.drain(new FluidStack(GlassHearts.inst.LIFEFORCE, (GlassHearts.inst.configGlassHeartCapacity-tegh.getLifeforce())-tegh.getLifeforceBuffer()), !worldIn.isRemote);
						if (rtrn != null) {
							if (rtrn.getFluid() == GlassHearts.inst.LIFEFORCE) {
								if (stack.getItem() == GlassHearts.inst.LIFEFORCE_BOTTLE) {
									stack.shrink(1);
									if (!playerIn.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
										playerIn.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
									}
								} else {
									playerIn.setHeldItem(hand, ifhi.getContainer());
								}
								tegh.setLifeforceBuffer(tegh.getLifeforceBuffer()+rtrn.amount);
								worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1f, 1f);
								return true;
							} else {
								LogManager.getLogger("GlassHearts").warn("We asked '{}' for '{}', but it gave us '{}' instead!", stack.getItem().getRegistryName(), GlassHearts.inst.LIFEFORCE.getUnlocalizedName(), rtrn.getFluid().getUnlocalizedName());
							}
						}
					}
				} catch (Throwable t) {
					// Some modders don't know null-side caps are valid
					// Rather than crashing, log the exception
					LogManager.getLogger("GlassHearts").warn("It seems the mod that owns the item '{}' doesn't know null-side caps are valid! This is a bug!", stack.getItem().getRegistryName(), t);
				}
			}
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity te = worldIn.getTileEntity(pos);
		if (!worldIn.isRemote && te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			if (stack.getItem() == Item.getItemFromBlock(GlassHearts.inst.GLASS_HEART)) {
				int meta = stack.getMetadata();
				EnumGlassColor egc = EnumGlassColor.values()[meta%EnumGlassColor.values().length];;
				tegh.setColor(egc);
				if (stack.hasDisplayName()) {
					tegh.setName(stack.getDisplayName());
				}
			}
		}
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (!worldIn.isRemote && te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			spawnAsEntity(worldIn, pos, getPickBlock(worldIn.getBlockState(pos), null, worldIn, pos, null));
			if (tegh.getGem() != EnumGem.NONE) {
				spawnAsEntity(worldIn, pos, new ItemStack(GlassHearts.inst.GEM, 1, ArrayUtils.indexOf(ItemGem.VALID_GEMS, tegh.getGem())));
			}
			GlassHeartWorldData.getDataFor(worldIn).remove(pos);
		}
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			ItemStack stack = new ItemStack(GlassHearts.inst.GLASS_HEART, 1, tegh.getColor().ordinal());
			if (tegh.getName() != null) {
				stack.setStackDisplayName(tegh.getName());
			}
			return stack;
		}
		return super.getPickBlock(state, target, world, pos, player);
	}
	
	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isTranslucent(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityGlassHeart();
	}
	
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < 17; i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}
	
}
