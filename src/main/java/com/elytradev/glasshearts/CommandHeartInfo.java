package com.elytradev.glasshearts;

import com.elytradev.glasshearts.capability.BasicHeartHandler;
import com.elytradev.glasshearts.capability.CapabilityHeartHandler;
import com.elytradev.glasshearts.capability.IHeartHandler;
import com.elytradev.glasshearts.logic.HeartContainer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandHeartInfo extends CommandBase {

	@Override
	public String getName() {
		return "glasshearts:info";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/glasshearts:info";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		boolean nbt = args.length == 1 && args[0].equals("nbt");
		boolean roundtrip = args.length == 1 && args[0].equals("roundtrip");
		if (sender instanceof Entity) {
			Entity e = (Entity)sender;
			if (e.hasCapability(CapabilityHeartHandler.CAPABILITY, null)) {
				IHeartHandler ihh = e.getCapability(CapabilityHeartHandler.CAPABILITY, null);
				sender.sendMessage(new TextComponentString("IHeartHandler Present"));
				if (roundtrip) {
					NBTBase tag = CapabilityHeartHandler.CAPABILITY.writeNBT(ihh, null);
					BasicHeartHandler bhh = new BasicHeartHandler();
					CapabilityHeartHandler.CAPABILITY.readNBT(bhh, null, tag);
					ihh = bhh;
				}
				if (nbt) {
					NBTTagCompound tag = (NBTTagCompound) CapabilityHeartHandler.CAPABILITY.writeNBT(ihh, null);
					sender.sendMessage(new TextComponentString(tag.toString()
							.replace("{", "§e{ §f")
							.replace("}", "§e }§f")
							.replace("[", "§e[ §f")
							.replace("]", "§e ]§f")
							.replace(",", "§6, §f")
							.replace("\"", "§d\"")
							.replaceAll(":(-?[0-9]+[bLfD]?)", ":§b$1§f")
							.replace(":", ": ")));
				} else {
					if (e instanceof EntityLivingBase) {
						EntityLivingBase elb = (EntityLivingBase)e;
						sender.sendMessage(new TextComponentString(elb.getHealth()+"/"+elb.getMaxHealth()+" vanilla health"));
						sender.sendMessage(new TextComponentString(elb.getAbsorptionAmount()+" absorption hearts"));
					}
					sender.sendMessage(new TextComponentString("Heart containers:"));
					float totalHp = 0;
					for (HeartContainer hc : ihh) {
						totalHp += hc.getFillAmount();
						sender.sendMessage(new TextComponentString("  "+hc));
						if (hc.getOwner() != null) {
							sender.sendMessage(new TextComponentString("    Owner: "+hc.getOwner()));
						}
					}
					sender.sendMessage(new TextComponentString("Total HP: "+totalHp+"/"+ihh.getContainers()));
				}
			} else if (e instanceof EntityLivingBase) {
				EntityLivingBase elb = (EntityLivingBase)e;
				sender.sendMessage(new TextComponentString("IHeartHandler NOT Present"));
				sender.sendMessage(new TextComponentString(elb.getHealth()+"/"+elb.getMaxHealth()+" vanilla health"));
				sender.sendMessage(new TextComponentString(elb.getAbsorptionAmount()+" absorption hearts"));
			} else {
				sender.sendMessage(new TextComponentString("IHeartHandler NOT Present"));
				sender.sendMessage(new TextComponentString("0/0 health (not a living entity)"));
			}
		} else {
			sender.sendMessage(new TextComponentString("0/0 health (not an entity)"));
		}
	}

}
