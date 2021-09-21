package com.varijon.tinies.BoostingElytra;

import com.pixelmonmod.pixelmon.Pixelmon;

import net.minecraft.nbt.NBTException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid="boostingelytra", version="1.0.6", acceptableRemoteVersions="*")
public class BoostingElytra
{
	public static String MODID = "modid";
	public static String VERSION = "version";
	boolean unlimitedBoost = false;
	
		
	@EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
	}
	
	@EventHandler
	public void init(FMLInitializationEvent e)
	{		
		MinecraftForge.EVENT_BUS.register(new BoostingElytraHandler(this));
		
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new UnlimitedBoostingCommand(this));
	}

	public boolean isUnlimitedBoost() 
	{
		return unlimitedBoost;
	}

	public void setUnlimitedBoost(boolean unlimitedBoost) 
	{
		this.unlimitedBoost = unlimitedBoost;
	}
}