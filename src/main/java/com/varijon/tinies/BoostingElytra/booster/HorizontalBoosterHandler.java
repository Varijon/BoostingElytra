package com.varijon.tinies.BoostingElytra.booster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class HorizontalBoosterHandler 
{
	MinecraftServer server;
	public HorizontalBoosterHandler()
	{
		server = FMLCommonHandler.instance().getMinecraftServerInstance();		
	}

	@SubscribeEvent
	public void onWorldTick (WorldTickEvent event)
	{
		try
		{
			if(event.phase != Phase.END)
			{
				return;
			}
			if(!event.world.getWorldInfo().getWorldName().equals("world"))
			{
				return;
			}
			for(Entity entity : event.world.getLoadedEntityList())
			{
				if(entity instanceof EntityArmorStand)
				{
					EntityArmorStand dataArmorStand = (EntityArmorStand) entity;
					NBTTagCompound standNBT = dataArmorStand.getEntityData();
					if(standNBT.hasKey("isHorizontalBooster"))
					{
						
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
