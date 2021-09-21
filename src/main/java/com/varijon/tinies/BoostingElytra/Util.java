package com.varijon.tinies.BoostingElytra;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

public class Util 
{	
	public static String GetFlightBarString(EntityPlayerMP player)
	{
		StringBuilder pipes = new StringBuilder();
		ItemStack angelRingItem = getAngelRing(player.inventory.mainInventory);
		ItemStack elytraItem = getElytra(player.getArmorInventoryList());
		if(angelRingItem == null)
		{
			float elytraMaxFlightTime = elytraItem.getTagCompound().getFloat("flightTime");
			float elytraFlightDuration = elytraItem.getTagCompound().getFloat("flightDuration");
			int whitePipes = (int) (((double)elytraFlightDuration / (double)elytraMaxFlightTime) * (double)50);
			pipes.append(TextFormatting.AQUA + "");
			for(int x = 0; x < whitePipes; x++)
			{
				pipes.append("|");
			}
			pipes.append(TextFormatting.DARK_GRAY);
			for(int x = 0; x < (50 - whitePipes); x++)
			{
				pipes.append("|");
			}
		}
		if(elytraItem == null)
		{
			float ringMaxFlightTime = angelRingItem.getTagCompound().getFloat("flightTime");
			float ringFlightDuration = angelRingItem.getTagCompound().getFloat("flightDuration");
			int otherLightPipes = (int) (((double)ringFlightDuration / (double)ringMaxFlightTime) * (double)50);
			pipes.append(TextFormatting.GOLD + "");
			for(int x = 0; x < otherLightPipes; x++)
			{
				pipes.append("|");
			}
			pipes.append(TextFormatting.DARK_GRAY);
			for(int x = 0; x < (50 - otherLightPipes); x++)
			{
				pipes.append("|");
			}
		}
		if(angelRingItem != null && elytraItem != null)
		{
			float elytraMaxFlightTime = elytraItem.getTagCompound().getFloat("flightTime");
			float elytraFlightDuration = elytraItem.getTagCompound().getFloat("flightDuration");
			int whitePipes = (int) (((double)elytraFlightDuration / (double)elytraMaxFlightTime) * (double)25);
			pipes.append(TextFormatting.DARK_GRAY);
			for(int x = 0; x < (25 - whitePipes); x++)
			{
				pipes.append("|");
			}
			pipes.append(TextFormatting.AQUA + "");
			for(int x = 0; x < whitePipes; x++)
			{
				pipes.append("|");
			}
			float ringMaxFlightTime = angelRingItem.getTagCompound().getFloat("flightTime");
			float ringFlightDuration = angelRingItem.getTagCompound().getFloat("flightDuration");
			int otherLightPipes = (int) (((double)ringFlightDuration / (double)ringMaxFlightTime) * (double)25);
			pipes.append(TextFormatting.GOLD + "");
			for(int x = 0; x < otherLightPipes; x++)
			{
				pipes.append("|");
			}
			pipes.append(TextFormatting.DARK_GRAY);
			for(int x = 0; x < (25 - otherLightPipes); x++)
			{
				pipes.append("|");
			}
		}
		//System.out.println("W: " + whitePipes + " D: " + (50-whitePipes) + " Flight: " + flightTime);
		return pipes.toString();
	}
	public static ItemStack getAngelRing(Iterable<ItemStack> itemList)
	{
		if(itemList != null)
		{
			for (ItemStack item : itemList) 
			{
				if(item != null)
				{
					if(item.hasTagCompound())
					{
						NBTTagCompound nbt = item.getTagCompound();
						if(nbt.hasKey("isAngelRing"))
						{
							return item;
						}
					}
				}
			}
		}
		return null;
	}
	public static ItemStack getElytra(Iterable<ItemStack> itemList)
	{
		if(itemList != null)
		{
			for (ItemStack item : itemList) 
			{
				if(item != null)
				{
					if(item.hasTagCompound())
					{
						NBTTagCompound nbt = item.getTagCompound();
						if(nbt.hasKey("isSuperiorElytra"))
						{
							return item;
						}
					}
				}
			}
		}
		return null;
	}
	public static String GetExpBarString(int currentExp, int maxExp)
	{
		int whitePipes = (int) (((double)currentExp / (double)maxExp) * (double)50);
		String pipes = TextFormatting.WHITE + "";
		for(int x = 0; x < whitePipes; x++)
		{
			pipes += "|";
		}
		pipes += TextFormatting.DARK_GRAY;
		for(int x = 0; x < (50 - whitePipes); x++)
		{
			pipes += "|";
		}
		return pipes;
	}
}
