package com.varijon.tinies.BoostingElytra;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class UnlimitedBoostingCommand implements ICommand {

	private List aliases;
	BoostingElytra boostingElytra;

	public UnlimitedBoostingCommand(BoostingElytra boostingElytra)
	{
	   this.aliases = new ArrayList();
	   this.aliases.add("unlimitboost");
	   this.boostingElytra = boostingElytra;
	}
	
	@Override
	public int compareTo(ICommand arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "unlimitboost";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "unlimitboost";
	}

	@Override
	public List<String> getAliases() {
		// TODO Auto-generated method stub
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(sender.canUseCommand(4, "boostingelytra.toggleunlimitboost"))
		{
			if(boostingElytra.isUnlimitedBoost())
			{			
				sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Disabled global unlimited elytra boosting"));
				boostingElytra.setUnlimitedBoost(false);
			}
			else
			{
				sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Enabled global unlimited elytra boosting"));
				boostingElytra.setUnlimitedBoost(true);
			}
			return;
		}
		else
		{
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have permission to use this command"));
			return;
		}

	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) 
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private String GetDateString(long millis)
	{
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		long day = (millis / (1000 * 60 * 60 * 24)) % 365;
		
		StringBuilder sb = new StringBuilder();
		if(day != 0)
		{
			if(day > 1)
			{
				sb.append(TextFormatting.GOLD + "" + day + TextFormatting.GREEN + " days ");				
			}
			else
			{
				sb.append(TextFormatting.GOLD + "" + day + TextFormatting.GREEN  + " day ");				
			}
		}
		if(hour != 0 || day != 0)
		{
			if(day != 0)
			{
				sb.append(", ");
			}
			if(hour > 1)
			{
				sb.append(TextFormatting.GOLD + "" + hour + TextFormatting.GREEN  + " hours ");				
			}
			else
			{
				sb.append(TextFormatting.GOLD + "" + hour + TextFormatting.GREEN  + " hour ");				
			}
		}
		if(minute != 0 || hour != 0 || day != 0)
		{
			if(hour != 0 || day != 0)
			{
				if(hour != 0 && day != 0)
				{
					sb.append(", ");
				}
				else
				{
					sb.append("and ");					
				}
			}
			if(minute > 1)
			{
				sb.append(TextFormatting.GOLD + "" + minute + TextFormatting.GREEN  + " minutes ");				
			}
			else
			{
				sb.append(TextFormatting.GOLD + "" + minute + TextFormatting.GREEN  + " minute ");				
			}
		}
		if(minute != 0 || hour != 0 || day != 0)
		{
			sb.append("and ");
		}
		if(second > 1)
		{
			sb.append(TextFormatting.GOLD + "" + second + TextFormatting.GREEN  + " seconds ago");				
		}
		else
		{
			sb.append(TextFormatting.GOLD + "" + second + TextFormatting.GREEN  + " second ago");				
		}	
		return sb.toString();
	}

}
