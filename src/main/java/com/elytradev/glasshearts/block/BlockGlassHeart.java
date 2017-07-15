package com.elytradev.glasshearts.block;

import org.apache.logging.log4j.LogManager;

import com.elytradev.glasshearts.GlassHearts;
import com.elytradev.glasshearts.capability.CapabilityHeartHandler;
import com.elytradev.glasshearts.capability.IHeartHandler;
import com.elytradev.glasshearts.enums.EnumGemState;
import com.elytradev.glasshearts.enums.EnumGlassColor;
import com.elytradev.glasshearts.gem.Gem;
import com.elytradev.glasshearts.init.Gems;
import com.elytradev.glasshearts.logic.BlockHeartContainerOwner;
import com.elytradev.glasshearts.logic.HeartContainer;
import com.elytradev.glasshearts.logic.HeartContainerOwner;
import com.elytradev.glasshearts.network.ParticleEffectMessage;
import com.elytradev.glasshearts.network.PlayHeartEffectMessage;
import com.elytradev.glasshearts.tile.TileEntityGlassHeart;
import com.elytradev.glasshearts.world.GlassHeartWorldData;
import com.google.common.base.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
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
		setSoundType(new SoundType(1f, 1f, SoundEvents.BLOCK_GLASS_HIT, SoundEvents.BLOCK_GLASS_STEP, SoundEvents.BLOCK_GLASS_PLACE, SoundEvents.BLOCK_GLASS_HIT, SoundEvents.BLOCK_GLASS_FALL));
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
	public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
		return true;
	}
	
	@Override
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		return true;
	}
	
	@Override
	public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
		return true;
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			return (int)(((tegh.getLifeforce()+tegh.getLifeforceBuffer())/(float)tegh.getLifeforceCapacity())*15);
		}
		return super.getComparatorInputOverride(blockState, worldIn, pos);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			if (stack.isEmpty() && tegh.getGem() != Gems.NONE) {
				if (tegh.getGem().getState(tegh) == EnumGemState.ACTIVE_CURSED) {
					return false;
				}
				if (!worldIn.isRemote) {
					spawnAsEntity(worldIn, pos, tegh.getGemStack());
					tegh.setGemStack(ItemStack.EMPTY);
					worldIn.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundCategory.BLOCKS, 1f, 1f);
				}
				return true;
			} else if (Gem.fromItemStack(stack) != Gems.NONE) {
				if (tegh.getGem().getState(tegh) == EnumGemState.ACTIVE_CURSED) {
					return false;
				}
				if (!worldIn.isRemote) {
					spawnAsEntity(worldIn, pos, tegh.getGemStack());
					worldIn.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundCategory.BLOCKS, 1f, 1f);
					tegh.setGemStack(stack.splitStack(1));
				}
				return true;
			} else if (stack.getItem() == GlassHearts.inst.STAFF) {
				if (worldIn.isRemote) return true;
				if (playerIn.hasCapability(CapabilityHeartHandler.CAPABILITY, null)) {
					IHeartHandler cap = playerIn.getCapability(CapabilityHeartHandler.CAPABILITY, null);
					for (int i = 0; i < cap.getContainers(); i++) {
						HeartContainer hc = cap.getContainer(i);
						HeartContainerOwner owner = hc.getOwner();
						if (owner instanceof BlockHeartContainerOwner) {
							BlockHeartContainerOwner bhco = (BlockHeartContainerOwner)owner;
							if (Objects.equal(bhco.getPos(), pos)) {
								if (tegh.getGem().getState(tegh) == EnumGemState.ACTIVE_CURSED) {
									return false;
								}
								cap.removeContainer(i);
								new PlayHeartEffectMessage(PlayHeartEffectMessage.EFFECT_UNATTUNE, tegh.getColor().ordinal(), i).sendTo(playerIn);
								return true;
							}
						}
					}
					if (cap.getContainers() < GlassHearts.inst.configMaxContainers) {
						cap.addContainer(HeartContainer.createGlass(tegh));
						stack.damageItem(1, playerIn);
						worldIn.playSound(null, pos, GlassHearts.inst.ATTUNE, SoundCategory.PLAYERS, 1f, 2f);
						new ParticleEffectMessage(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, playerIn, ParticleEffectMessage.EFFECT_ATTUNE).sendToAllWatchingAndSelf(playerIn);
					} else {
						playerIn.sendStatusMessage(new TextComponentTranslation("msg.glasshearts.limitReached"), true);
					}
					return true;
				} else {
					return false;
				}
			} else if (stack.getItem() != GlassHearts.inst.LIFEFORCE_BOTTLE && tegh.getLifeforceBuffer() < tegh.getLifeforceCapacity()) {
				try {
					if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
						IFluidHandlerItem ifhi = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
						FluidStack rtrn = ifhi.drain(new FluidStack(GlassHearts.inst.LIFEFORCE, (tegh.getLifeforceCapacity()-tegh.getLifeforce())-tegh.getLifeforceBuffer()), !worldIn.isRemote);
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
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		if (!world.isRemote) {
			TileEntity te = world.getChunkFromBlockCoords(pos).getTileEntity(pos, EnumCreateEntityType.CHECK);
			if (!world.isRemote && te instanceof TileEntityGlassHeart) {
				TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
				if (tegh.getLifeforce()+tegh.getLifeforceBuffer() > 1000) {
					world.removeTileEntity(pos);
					world.setBlockState(pos, GlassHearts.inst.LIFEFORCE_BLOCK.getDefaultState());
				} else {
					world.removeTileEntity(pos);
					world.setBlockToAir(pos);
				}
			}
			GlassHeartWorldData.getDataFor(world).remove(pos);
		}
		onBlockDestroyedByExplosion(world, pos, explosion);
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			if (tegh.getGem().getState(tegh) == EnumGemState.ACTIVE_CURSED) {
				return -1;
			}
		}
		return super.getBlockHardness(blockState, worldIn, pos);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity te = worldIn.getTileEntity(pos);
		if (!worldIn.isRemote && te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			if (stack.getItem() == Item.getItemFromBlock(GlassHearts.inst.GLASS_HEART)) {
				int meta = stack.getMetadata();
				EnumGlassColor egc = EnumGlassColor.values()[meta%EnumGlassColor.values().length];
				tegh.setColor(egc);
				if (stack.hasDisplayName()) {
					tegh.setName(stack.getDisplayName());
				}
			}
		}
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		boolean placeLiquid = false;
		TileEntity te = worldIn.getChunkFromBlockCoords(pos).getTileEntity(pos, EnumCreateEntityType.CHECK);
		if (!worldIn.isRemote && te instanceof TileEntityGlassHeart) {
			TileEntityGlassHeart tegh = (TileEntityGlassHeart)te;
			spawnAsEntity(worldIn, pos, getPickBlock(worldIn.getBlockState(pos), null, worldIn, pos, null));
			spawnAsEntity(worldIn, pos, tegh.getGemStack());
			if (tegh.getLifeforce()+tegh.getLifeforceBuffer() > 1000) {
				placeLiquid = true;
			}
			if (tegh.getColor() == EnumGlassColor.NONE) {
				worldIn.playEvent(2001, pos, Block.getIdFromBlock(Blocks.GLASS));
			} else {
				worldIn.playEvent(2001, pos, (tegh.getColor().ordinal()-1 << 12) | Block.getIdFromBlock(Blocks.STAINED_GLASS));
			}
			GlassHeartWorldData.getDataFor(worldIn).remove(pos);
		}
		super.breakBlock(worldIn, pos, state);
		if (placeLiquid) {
			worldIn.setBlockState(pos, GlassHearts.inst.LIFEFORCE_BLOCK.getDefaultState());
		}
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
	public boolean canDropFromExplosion(Explosion explosionIn) {
		return false;
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
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < 17; i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}
	
}
