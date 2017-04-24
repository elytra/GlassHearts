package com.elytradev.glasshearts.item;

import com.elytradev.glasshearts.GlassHearts;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class ItemLifeforceBottle extends Item {

	public class DispenseLifeforceBottle extends BehaviorDefaultDispenseItem {
		@Override
		protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
			EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
			TileEntity te = source.getBlockTileEntity().getWorld().getTileEntity(source.getBlockPos().offset(facing));
			EnumActionResult result = fill(stack, facing.getOpposite(), te);
			if (result == EnumActionResult.SUCCESS) {
				stack.shrink(1);
				ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
				if (((TileEntityDispenser)source.getBlockTileEntity()).addItemStack(bottle) == -1) {
					super.dispenseStack(source, bottle);
				}
				return stack;
			}
			return result == EnumActionResult.PASS ? super.dispenseStack(source, stack) : stack;
		}
	}

	public class CapabilityProvider implements ICapabilityProvider {

		private ItemStack stack;
		
		public CapabilityProvider(ItemStack stack) {
			this.stack = stack;
		}
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
				return (T)new IFluidHandlerItem() {
					
					@Override
					public IFluidTankProperties[] getTankProperties() {
						return new IFluidTankProperties[] {
								new FluidTankProperties(new FluidStack(GlassHearts.inst.LIFEFORCE, GlassHearts.inst.configLifeforceBottleSize), GlassHearts.inst.configLifeforceBottleSize, false, true)
						};
					}
					
					@Override
					public int fill(FluidStack resource, boolean doFill) {
						return 0;
					}
					
					@Override
					public FluidStack drain(int maxDrain, boolean doDrain) {
						if (maxDrain >= GlassHearts.inst.configLifeforceBottleSize) {
							if (doDrain) {
								stack.shrink(1);
							}
							return new FluidStack(GlassHearts.inst.LIFEFORCE, GlassHearts.inst.configLifeforceBottleSize);
						}
						return null;
					}
					
					@Override
					public FluidStack drain(FluidStack resource, boolean doDrain) {
						if (resource != null && resource.getFluid() == GlassHearts.inst.LIFEFORCE) {
							return drain(resource.amount, doDrain);
						}
						return null;
					}
					
					@Override
					public ItemStack getContainer() {
						return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
					}
				};
			}
			return null;
		}

	}

	public ItemLifeforceBottle() {
		setUnlocalizedName("glasshearts.lifeforce_bottle");
		setMaxStackSize(16);
		setContainerItem(Items.GLASS_BOTTLE);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new DispenseLifeforceBottle());
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CapabilityProvider(stack);
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 16;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		playerIn.setActiveHand(handIn);
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		ItemStack stack = player.getHeldItem(hand);
		EnumActionResult result = fill(stack, facing, te);
		if (result == EnumActionResult.SUCCESS) {
			if (!worldIn.isRemote) {
				stack.shrink(1);
				if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
					player.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
				}
			}
		}
		return result;
	}
	
	private EnumActionResult fill(ItemStack stack, EnumFacing facing, TileEntity te) {
		if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
			if (!te.getWorld().isRemote) {
				IFluidHandler ifh = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
				int amt = ifh.fill(new FluidStack(GlassHearts.inst.LIFEFORCE, GlassHearts.inst.configLifeforceBottleSize), false);
				if (amt > 20 && amt <= GlassHearts.inst.configLifeforceBottleSize) {
					ifh.fill(new FluidStack(GlassHearts.inst.LIFEFORCE, GlassHearts.inst.configLifeforceBottleSize), true);
					te.getWorld().playSound(null, te.getPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1f, 1f);
					return EnumActionResult.SUCCESS;
				}
				return EnumActionResult.FAIL;
			} else {
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		entityLiving.heal(4f);
		stack.shrink(1);
		if (entityLiving instanceof EntityPlayer) {
			if (!((EntityPlayer) entityLiving).inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
				entityLiving.dropItem(Items.GLASS_BOTTLE, 1);
			}
		} else {
			entityLiving.dropItem(Items.GLASS_BOTTLE, 1);
		}
		return super.onItemUseFinish(stack, worldIn, entityLiving);
	}
	
}
