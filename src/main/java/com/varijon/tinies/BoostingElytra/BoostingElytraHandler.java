package com.varijon.tinies.BoostingElytra;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.world.ParticleArcaneryDispatcher;
import com.pixelmonmod.pixelmon.battles.attacks.TargetingInfo;
import com.pixelmonmod.pixelmon.battles.status.Electrify;
import com.pixelmonmod.pixelmon.client.particle.Particles;
import com.pixelmonmod.pixelmon.client.particle.systems.Shiny;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class BoostingElytraHandler 
{
	int counter = 0;
	MinecraftServer server;
	static double EXPCONSTANT = 0.3;
	static double MAX_FLIGHT_ACC = 0.038;
	static double MAX_FLIGHT_TIME = 30;
	static double MAX_FLIGHT_REC = 0.8;
	BoostingElytra boostingElytra;
	int tickTPSCount = 0;
	long oldTime = System.currentTimeMillis();
	float tps = 20;
	
	ArrayList<BoostingElytraTier> elytraTiers;
	
	public BoostingElytraHandler(BoostingElytra boostingElytra)
	{
		server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		elytraTiers = new ArrayList<BoostingElytraTier>();
		elytraTiers.add(new BoostingElytraTier(TextFormatting.WHITE + "Common", 50, 2.5f, 25f, 0.015f, 0.060f, 0.05f, 0.15f, 10, new String[]{"witchMagic","fireworksSpark","magicCrit","splash","instantSpell","note","enchantmenttable","dripWater","dripLava"}));
		elytraTiers.add(new BoostingElytraTier(TextFormatting.GREEN + "Uncommon", 30, 3f, 35f, 0.020f, 0.075f, 0.1f, 0.25f, 20, new String[]{"witchMagic","fireworksSpark","magicCrit","splash","instantSpell","enchantmenttable","note","dripWater","dripLava","heart","damageIndicator","dragonbreath","smoke","lava","reddust","endRod"}));
		elytraTiers.add(new BoostingElytraTier(TextFormatting.YELLOW + "Rare", 10, 4.5f, 50f, 0.030f, 0.090f, 0.15f, 0.35f, 50, new String[]{"witchMagic","fireworksSpark","magicCrit","splash","instantSpell","enchantmenttable","note","dripWater","dripLava","heart","damageIndicator","dragonbreath","smoke","lava","reddust","endRod"}));
		elytraTiers.add(new BoostingElytraTier(TextFormatting.GOLD + "Legendary", 5, 5.5f, 80f, 0.035f, 0.120f, 0.20f, 0.45f, 100, new String[]{"witchMagic","fireworksSpark","magicCrit","splash","instantSpell","enchantmenttable","note","dripWater","dripLava","heart","damageIndicator","dragonbreath","smoke","lava","reddust","endRod"}));
		
		this.boostingElytra = boostingElytra;
	}
	
	public BoostingElytraTier getRandomElytraTier()
	{
		Random rng = new Random();
		BoostingElytraTier elytraTier = null;
		while(elytraTier == null)
		{
			int randomNumber = rng.nextInt(101);
			int randomElement = rng.nextInt(elytraTiers.size());
			if(elytraTiers.get(randomElement).getRarity() >= randomNumber)
			{
				elytraTier = elytraTiers.get(randomElement);
			}
		}
		return elytraTier;
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
			if(tickTPSCount >= 20)
			{
				tickTPSCount = 0;
				long newTime = System.currentTimeMillis();
				long timeDiff = newTime - oldTime;
				oldTime = newTime;
				tps = 20 * (1000f / timeDiff);
				if(tps > 20)
				{
					tps = 20;
				}
			}
			tickTPSCount++;
			if(counter == 1)
			{
				for(EntityPlayerMP targetPlayer : server.getPlayerList().getPlayers())
				{
					ItemStack elytra = GetSuperiorElytra(targetPlayer.getArmorInventoryList());
					if(elytra != null)
					{
						NBTTagCompound nbt = elytra.getTagCompound();
						float maxFlightTime = nbt.getFloat("flightTime");
						float flightAcceleration = nbt.getFloat("flightAcceleration");
						float flightTimeRecovery = nbt.getFloat("flightRecovery");
						String flightParticle = nbt.getString("flightParticle");
						float flightTime = nbt.getFloat("flightDuration");
						double partOffset = nbt.getDouble("partOffset");
						double partSpeed = nbt.getDouble("partSpeed");
						String blockData = "minecraft:air";
						if(nbt.hasKey("extraValue"))
						{
							if(!nbt.getString("extraValue").contains("minecraft:air"))
							{
								blockData = nbt.getString("extraValue");
							}
						}
						
						int partCount = nbt.getInteger("partCount");
						boolean depthDiver = nbt.getBoolean("depthDiver");
						
						
						boolean addXP = false;
						
						if(targetPlayer.isInWater() && depthDiver && targetPlayer.isElytraFlying())
						{
							flightAcceleration *= 10;
							targetPlayer.getServerWorld().spawnParticle(EnumParticleTypes.WATER_BUBBLE, targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 10, 0.5, 0.5, 0.5, 0, new int[]{});
	
						}
						
						if(targetPlayer.isElytraFlying() && targetPlayer.isSneaking())
						{
							if(flightTime > maxFlightTime)
							{
								flightTime = maxFlightTime;
							}
							if(flightTime > 0)
							{
								float yaw = targetPlayer.rotationYaw;
								float pitch = targetPlayer.rotationPitch;
								float tpsMultiplier = (20f / tps);
								if(tpsMultiplier > 2)
								{
									tpsMultiplier = 2;
								}
								float f = flightAcceleration * tpsMultiplier;
								double motionX = (double)(-MathHelper.sin(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * f);
								double motionZ = (double)(MathHelper.cos(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * f);
								double motionY = (double)(-MathHelper.sin((pitch) / 180.0F * (float)Math.PI) * f);
								targetPlayer.addVelocity(motionX, motionY, motionZ);
								targetPlayer.velocityChanged = true;
								//flightTimeCount.put(targetPlayer.getUniqueID().toString(), flightTime - 1);
								if(!boostingElytra.isUnlimitedBoost())
								{
									flightTime -= 0.05F;
									nbt.setFloat("flightDuration", flightTime);									
								}
								

								if(EnumParticleTypes.getByName(flightParticle) != null)
								{										
									targetPlayer.getServerWorld().spawnParticle(EnumParticleTypes.getByName(flightParticle), targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, partCount, 1.5, 1.5, 1.5, partSpeed, Block.getStateId(Block.getBlockFromName(blockData).getDefaultState()));							
								}
								else
								{
									if(flightParticle.contains("Shiny"))
									{
										ArrayList<Double> argList = new ArrayList<Double>();
										argList.add(partOffset);
										argList.add(partOffset);
										argList.add(partOffset);
										for(int x = partCount; x > 0; x--)
										{
											ParticleArcaneryDispatcher.dispatchToDimension(targetPlayer.getServerWorld().getWorldType().getId(), 50, targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 0, 0, 0, 1, Particles.Shiny, argList.toArray());
										}									
									}	
									if(flightParticle.contains("Electric"))
									{
										ArrayList<Integer> argList = new ArrayList<>();
										argList.add((5 + event.world.rand.nextInt(10)));
										argList.add(1);
										argList.add(1);
										argList.add(1);
										for(int x = partCount; x > 0; x--)
										{
											ParticleArcaneryDispatcher.dispatchToDimension(targetPlayer.getServerWorld().getWorldType().getId(), 50, targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 0, 0, 0, 1, Particles.Electric, argList.toArray());
										}									
									}
									if(flightParticle.contains("BlueMagic"))
									{
										ArrayList<Double> argList = new ArrayList<Double>();
										argList.add(2.0);
										argList.add(0.8);
										argList.add(2.0);
										for(int x = partCount; x > 0; x--)
										{
											ParticleArcaneryDispatcher.dispatchToDimension(targetPlayer.getServerWorld().getWorldType().getId(), 50, targetPlayer.posX + RandomHelper.getRandomNumberBetween(1f,(float) (partOffset+1f)), targetPlayer.posY + RandomHelper.getRandomNumberBetween(1f,(float) (partOffset+1f)), targetPlayer.posZ + RandomHelper.getRandomNumberBetween(1f, (float) (partOffset+1f)), 0, 0, 0, 1, Particles.BlueMagic, argList.toArray());
										}										
									}	
								}								
								addXP = true;
							}
						}
						if(!targetPlayer.onGround && targetPlayer.isSneaking() && !targetPlayer.isElytraFlying() && targetPlayer.rotationPitch < -30)
						{
							BlockPos pos2 = targetPlayer.getPosition();
							World world = targetPlayer.getServerWorld();
							if(world.getBlockState(pos2).getBlock() == Blocks.LADDER || world.getBlockState(pos2).getBlock() == Blocks.VINE)
							{
								return;
							}
							if(GetElytraLaunchBoots(targetPlayer.getArmorInventoryList()) && !targetPlayer.capabilities.isFlying)
							{
								if(flightTime > maxFlightTime)
								{
									flightTime = maxFlightTime;
								}
								float amountToTake = (maxFlightTime / 100f) * 30f;
								if(flightTime > amountToTake)
								{
									if(targetPlayer.isInWater() && depthDiver)
									{
										flightAcceleration /=10;
									}
									
									float yaw = targetPlayer.rotationYaw;
									float pitch = targetPlayer.rotationPitch;
									float f = flightAcceleration * 15;
									double motionX = (double)(-MathHelper.sin(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * f);
									double motionZ = (double)(MathHelper.cos(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * f);
									double motionY = (double)(-MathHelper.sin((pitch) / 180.0F * (float)Math.PI) * f);
									targetPlayer.addVelocity(motionX, motionY, motionZ);
									targetPlayer.velocityChanged = true;
									targetPlayer.onGround = false;
									targetPlayer.setElytraFlying();
									//flightTimeCount.put(targetPlayer.getUniqueID().toString(), flightTime - 1);
									if(!boostingElytra.isUnlimitedBoost())
									{
										nbt.setFloat("flightDuration", flightTime - amountToTake);
									}
									
	//								if(flightParticle.contains("rainbowReddust"))
	//								{
	//									double speed = 0.2;
	//									flightParticle = "reddust";
	//									targetPlayer.getServerWorld().spawnParticle(EnumParticleTypes.getByName(flightParticle), targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 20, 0.5, 0.5, 0.5, speed, new int[]{});
	//
	//								}
	//								else if(flightParticle.contains("rainbowMobSpell"))
	//								{
	//									double speed = 0.2;
	//									flightParticle = "mobSpell";
	//									targetPlayer.getServerWorld().spawnParticle(EnumParticleTypes.getByName(flightParticle), targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 20, 1.0, 1.0, 1.0, speed, new int[]{});								
	//								}
	//								else if(flightParticle.contains("sandDust"))
	//								{
	//									double speed = 0;
	//									flightParticle = "fallingdust";
	//									targetPlayer.getServerWorld().spawnParticle(EnumParticleTypes.getByName(flightParticle), targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 20, 0.7, 0.7, 0.7, speed, new int[]{80});								
	//								}
	//								else if(flightParticle.contains("rainbowMobSpellAmbient"))
	//								{
	//									double speed = 0.2;
	//									flightParticle = "mobSpellAmbient";
	//									targetPlayer.getServerWorld().spawnParticle(EnumParticleTypes.getByName(flightParticle), targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 20, 1.0, 1.0, 1.0, speed, new int[]{});								
	//								}
	//								else
	//								{
	//									targetPlayer.getServerWorld().spawnParticle(EnumParticleTypes.getByName(flightParticle), targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 20, 1, 1, 1, 0, new int[]{});
	//								}
									if(EnumParticleTypes.getByName(flightParticle) != null)
									{										
										targetPlayer.getServerWorld().spawnParticle(EnumParticleTypes.getByName(flightParticle), targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 50, 1.5, 1.5, 1.5, partSpeed, Block.getStateId(Block.getBlockFromName(blockData).getDefaultState()));							
									}
									else
									{
										if(flightParticle.contains("Shiny"))
										{
											ArrayList<Double> argList = new ArrayList<Double>();
											argList.add(2.5);
											argList.add(2.5);
											argList.add(2.5);
											for(int x = 50; x > 0; x--)
											{
												ParticleArcaneryDispatcher.dispatchToDimension(targetPlayer.getServerWorld().getWorldType().getId(), 50, targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 0, 0, 0, 1, Particles.Shiny, argList.toArray());
											}										
										}	
										if(flightParticle.contains("BlueMagic"))
										{
											ArrayList<Double> argList = new ArrayList<Double>();
											argList.add(2.0);
											argList.add(0.8);
											argList.add(2.0);
											for(int x = 50; x > 0; x--)
											{
												ParticleArcaneryDispatcher.dispatchToDimension(targetPlayer.getServerWorld().getWorldType().getId(), 50, targetPlayer.posX + RandomHelper.getRandomNumberBetween(1f, 2.5f), targetPlayer.posY + RandomHelper.getRandomNumberBetween(1f, 2.5f), targetPlayer.posZ + RandomHelper.getRandomNumberBetween(1f, 2.5f), 0, 0, 0, 1, Particles.BlueMagic, argList.toArray());
											}										
										}								
									}
								}
							}
						}
						if(targetPlayer.onGround && flightTime < maxFlightTime)
						{
							nbt.setFloat("flightDuration", flightTime + flightTimeRecovery);
							if(flightTime + flightTimeRecovery >= maxFlightTime)
							{
								nbt.setBoolean("finishCharging", true);
							}
						}
						if (targetPlayer.isInWater() && flightTime < maxFlightTime && depthDiver && targetPlayer.isElytraFlying())
						{
							nbt.setFloat("flightDuration", flightTime + (flightTimeRecovery / 10.0f));
						}
						if(flightTime < maxFlightTime)
						{
	//						SPacketTitle packetTimes = new SPacketTitle(0, 5, 5);
	//						SPacketTitle packetSubtitle = new SPacketTitle(SPacketTitle.Type.SUBTITLE, new TextComponentString(GetFlightBarString(flightTime, maxFlightTime)));
	//						SPacketTitle packetTitle = new SPacketTitle(SPacketTitle.Type.TITLE, new TextComponentString(""));
	//
	//						targetPlayer.connection.sendPacket(packetTimes);
	//						targetPlayer.connection.sendPacket(packetTitle);	
	//						targetPlayer.connection.sendPacket(packetSubtitle);	
							
							SPacketChat chatPacket = new SPacketChat(new TextComponentString(Util.GetFlightBarString(targetPlayer)),ChatType.GAME_INFO);
							targetPlayer.connection.sendPacket(chatPacket);													
						}
						if(nbt.hasKey("finishCharging"))
						{
							if(nbt.getBoolean("finishCharging"))
							{
								SPacketChat chatPacket = new SPacketChat(new TextComponentString(Util.GetFlightBarString(targetPlayer)),ChatType.GAME_INFO);
								targetPlayer.connection.sendPacket(chatPacket);		
								nbt.setBoolean("finishCharging", false);
							}
						}
						
						if(nbt.hasKey("isLevelingElytra"))
						{
							if(addXP)
							{
								int elytraLevel = -1;
								double elytraCurrentXP = -1;
								int elytraRequiredXP = -1;
								if(nbt.hasKey("elytraLevel"))
								{
									elytraLevel = nbt.getInteger("elytraLevel");
								}
								if(nbt.hasKey("elytraCurrentXP"))
								{
									elytraCurrentXP = nbt.getDouble("elytraCurrentXP");
								}
								if(nbt.hasKey("elytraRequiredXP"))
								{
									elytraRequiredXP = nbt.getInteger("elytraRequiredXP");
								}
								double newXP = elytraCurrentXP + 0.1d;
								if(newXP >= elytraRequiredXP)
								{
									int newElytraLevel = elytraLevel + 1;
									newXP = newXP - (double)elytraRequiredXP;
									nbt.setInteger("elytraLevel", newElytraLevel);
									nbt.setInteger("elytraRequiredXP", (int) Math.pow(((double) (elytraLevel + 1) / EXPCONSTANT), 2));
									
									StringBuilder sb = new StringBuilder();
									sb.append(TextFormatting.GREEN + "Elytra is now level " + TextFormatting.RED + newElytraLevel + TextFormatting.GREEN + "!" + TextFormatting.GOLD + " Gained: ");
									
									if(newElytraLevel % 5 == 0 && flightAcceleration < MAX_FLIGHT_ACC)
									{
										nbt.setFloat("flightAcceleration", flightAcceleration + 0.01f);
										sb.append(TextFormatting.RED + "0.01 " + TextFormatting.GOLD + "Flight Acceleration, ");
									}
									if(newElytraLevel % 5 == 0 && flightTimeRecovery < MAX_FLIGHT_REC)
									{
										nbt.setFloat("flightRecovery", flightTimeRecovery + 0.02f);
										sb.append(TextFormatting.RED + "0.02 " + TextFormatting.GOLD + "Flight Recovery, ");
									}
									nbt.setFloat("flightTime", maxFlightTime + 0.1f);
									sb.append(TextFormatting.RED + "0.1 " + TextFormatting.GOLD + "Flight Time");
									targetPlayer.sendMessage(new TextComponentString(sb.toString()));
									
								}
								nbt.setDouble("elytraCurrentXP", newXP);
							}
						}
						
						
						elytra.setTagCompound(nbt);
					}
				}
				counter = 0;
				return;
			}
			counter++;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void onItemClick(PlayerInteractEvent.RightClickItem event)
	{
		EntityPlayerMP targetPlayer = (EntityPlayerMP) event.getEntityPlayer();
		ItemStack elytra = GetSuperiorElytra(targetPlayer.getArmorInventoryList());
		ItemStack book = targetPlayer.getHeldItem(EnumHand.MAIN_HAND);
		if(checkForParticleBook(event.getItemStack()))
		{
			if(elytra != null)
			{
				NBTTagCompound nbtElytra = elytra.getTagCompound();
				NBTTagCompound nbtBook = book.getTagCompound();
							
				ItemStack bookToDrop = new ItemStack(Items.ENCHANTED_BOOK);
				NBTTagCompound bookCompound = new NBTTagCompound();
				bookCompound.setTag("display", new NBTTagCompound());
				NBTTagList lstLore = new NBTTagList();
				boolean particleReplaced = false;
				boolean dropBook = true;
				
				if(nbtBook.hasKey("flightParticle"))
				{
					bookCompound.setString("flightParticle",nbtElytra.getString("flightParticle"));
					nbtElytra.setString("flightParticle",nbtBook.getString("flightParticle"));
					particleReplaced = true;
				}
				if(nbtBook.hasKey("partOffset"))
				{
					bookCompound.setDouble("partOffset", nbtElytra.getDouble("partOffset"));
					nbtElytra.setDouble("partOffset", nbtBook.getDouble("partOffset"));							
				}
				if(nbtBook.hasKey("partSpeed"))
				{
					bookCompound.setDouble("partSpeed",  nbtElytra.getDouble("partSpeed"));	
					nbtElytra.setDouble("partSpeed",  nbtBook.getDouble("partSpeed"));						
				}
				if(nbtBook.hasKey("partCount"))
				{
					bookCompound.setInteger("partCount", nbtElytra.getInteger("partCount"));	
					nbtElytra.setInteger("partCount", nbtBook.getInteger("partCount"));					
				}
				if(nbtBook.hasKey("depthDiver"))
				{
					bookCompound.setBoolean("depthDiver", nbtElytra.getBoolean("depthDiver"));	
					nbtElytra.setBoolean("depthDiver", nbtBook.getBoolean("depthDiver"));				
				}
				if(nbtBook.hasKey("flightUpgradePercent"))
				{
					float newMaxFlight = nbtElytra.getFloat("flightTime") * ((nbtBook.getFloat("flightUpgradePercent") / 100.0f) + 1f);
					nbtElytra.setFloat("flightTime", newMaxFlight);					
				}
				if(nbtBook.hasKey("flightRecoverUpgradePercent"))
				{
					float newMaxFlight = nbtElytra.getFloat("flightRecovery") * ((nbtBook.getFloat("flightRecoverUpgradePercent") / 100.0f) + 1f);
					nbtElytra.setFloat("flightRecovery", newMaxFlight);					
				}
				if(nbtBook.hasKey("extraValue"))
				{
					bookCompound.setString("extraValue", nbtElytra.getString("extraValue"));
					nbtElytra.setString("extraValue", nbtBook.getString("extraValue"));		
				}
				if(!nbtBook.hasKey("extraValue") && nbtBook.hasKey("flightParticle"))
				{		
					bookCompound.setString("extraValue", nbtElytra.getString("extraValue"));	
					nbtElytra.setString("extraValue", "minecraft:air");								
				}
				
				if(particleReplaced)
				{
					bookCompound.getCompoundTag("display").setString("Name", TextFormatting.GOLD + "Elytra Particle Book");
				}
				else
				{
					bookCompound.getCompoundTag("display").setString("Name", TextFormatting.GOLD + "Elytra Particle Config Book");					
				}
				
				if(bookCompound.hasKey("flightParticle"))
				{
					if(bookCompound.hasKey("extraValue"))
					{
						if(!bookCompound.getString("extraValue").contains("minecraft:air"))
						{
							lstLore.appendTag(new NBTTagString(TextFormatting.AQUA + "Particle: " + TextFormatting.GOLD + bookCompound.getString("extraValue")));							
						}
						else
						{
							lstLore.appendTag(new NBTTagString(TextFormatting.AQUA + "Particle: " + TextFormatting.GOLD + bookCompound.getString("flightParticle")));								
						}
					}
					else
					{
						lstLore.appendTag(new NBTTagString(TextFormatting.AQUA + "Particle: " + TextFormatting.GOLD + bookCompound.getString("flightParticle")));						
					}
				}
				if(bookCompound.hasKey("partOffset"))
				{							
					lstLore.appendTag(new NBTTagString(TextFormatting.AQUA + "Particle Offset: " + TextFormatting.GOLD + bookCompound.getDouble("partOffset")));
				}
				if(bookCompound.hasKey("partSpeed"))
				{	
					lstLore.appendTag(new NBTTagString(TextFormatting.AQUA + "Particle Speed: " + TextFormatting.GOLD + bookCompound.getDouble("partSpeed")));				
				}
				if(bookCompound.hasKey("partCount"))
				{		
					lstLore.appendTag(new NBTTagString(TextFormatting.AQUA + "Particle Count: " + TextFormatting.GOLD + bookCompound.getInteger("partCount")));		
				}
				if(bookCompound.hasKey("depthDiver"))
				{		
					lstLore.appendTag(new NBTTagString(TextFormatting.AQUA + "Depth Diver: " + TextFormatting.GOLD + bookCompound.getBoolean("depthDiver")));	
					dropBook = false;
				}
				bookCompound.setInteger("isParticleBook", 1);
				bookCompound.getCompoundTag("display").setTag("Lore", lstLore);
				bookToDrop.setTagCompound(bookCompound);
				//(!nbt.getString("extraValue").contains("minecraft:air") ? TextFormatting.GRAY + nbt.getString("extraValue") : nbt.getString("flightParticle") + " Particle Book");	
	
				if(targetPlayer.getHeldItem(EnumHand.MAIN_HAND).getCount() > 1)
				{
					targetPlayer.getHeldItem(EnumHand.MAIN_HAND).setCount(targetPlayer.getHeldItem(EnumHand.MAIN_HAND).getCount() - 1);
				}
				else
				{
					targetPlayer.inventory.removeStackFromSlot(targetPlayer.inventory.currentItem);
				}
				
				if(dropBook)
				{
					if(!targetPlayer.inventory.addItemStackToInventory(bookToDrop))
					{
						World w = targetPlayer.getEntityWorld();
						w.spawnEntity(new EntityItem(w, targetPlayer.lastTickPosX, targetPlayer.lastTickPosY, targetPlayer.lastTickPosZ, bookToDrop));
					}
				}
					
				targetPlayer.inventoryContainer.detectAndSendChanges();
				elytra.setTagCompound(nbtElytra);
				
				NBTTagList loreList = new NBTTagList();
				
				DecimalFormat df = new DecimalFormat("###.##");
				DecimalFormat df3 = new DecimalFormat("###.#");
				
				boolean customLore = true;
				if(nbtElytra.getCompoundTag("display").hasKey("Lore"))
				{
					for(NBTBase loreTag : nbtElytra.getCompoundTag("display").getTagList("Lore", Constants.NBT.TAG_STRING))
					{
						if(((NBTTagString)loreTag).getString().contains("Flight Time:"))
						{
							customLore = false;
						}
					}
				}
				if(!customLore)
				{
					if(!nbtElytra.hasKey("isLevelingElytra"))
					{
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Tier: " + TextFormatting.GRAY + nbtElytra.getString("flightTier")));							
					}
					else
					{
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Level: " + TextFormatting.GRAY + nbtElytra.getInteger("elytraLevel")));
					}
					loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Flight Time: " + TextFormatting.GRAY + df.format(nbtElytra.getFloat("flightTime"))));
					loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Flight Acceleration: " + TextFormatting.GRAY + nbtElytra.getFloat("flightAcceleration")));
					loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Flight Recovery: " + TextFormatting.GRAY + df.format(nbtElytra.getFloat("flightRecovery"))));
					loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Boost Particle: " + TextFormatting.GRAY + (!nbtElytra.getString("extraValue").contains("minecraft:air") ? TextFormatting.GRAY + nbtElytra.getString("extraValue") : nbtElytra.getString("flightParticle"))));
					loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Depth Diver: " + TextFormatting.GRAY + nbtElytra.getBoolean("depthDiver")));
					loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Particle Count: " + TextFormatting.GRAY + nbtElytra.getInteger("partCount")));
					loreList.appendTag(new NBTTagString(TextFormatting.GRAY + "Hold Sneak to boost while flying"));
					loreList.appendTag(new NBTTagString(TextFormatting.GRAY + "Hold Sneak and jump while looking up to take off"));
	
	
					if(nbtElytra.hasKey("isLevelingElytra"))
					{
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Exp Required: " + TextFormatting.GRAY + "(" + df3.format(nbtElytra.getDouble("elytraCurrentXP")) + "/" + nbtElytra.getInteger("elytraRequiredXP") + ")"));
						loreList.appendTag(new NBTTagString(Util.GetExpBarString((int) nbtElytra.getDouble("elytraCurrentXP"), nbtElytra.getInteger("elytraRequiredXP"))));
					}				
					nbtElytra.getCompoundTag("display").setTag("Lore", loreList);
				}
				
				
				elytra.setTagCompound(nbtElytra);
				targetPlayer.sendMessage(new TextComponentString(TextFormatting.GREEN + "Elytra updated"));
			}
			else
			{
				targetPlayer.sendMessage(new TextComponentString(TextFormatting.RED + "No compatible Elytra found in chest slot"));
			}
		}
	}
		

	public boolean checkForParticleBook(ItemStack item)
	{
		if(item != null)
		{
			NBTTagCompound nbt = item.getTagCompound();
			if(nbt != null)
			{
				if(nbt.hasKey("isParticleBook"))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean GetElytraLaunchBoots(Iterable<ItemStack> itemList)
	{
//		if(itemList != null)
//		{
//			for (ItemStack item : itemList) 
//			{
//				if(item != null)
//				{
//					if(item.hasTagCompound())
//					{
//						NBTTagCompound nbt = item.getTagCompound();
//						if(nbt.hasKey("isElytraLaunchBoots"))
//						{
//							return true;
//						}
//					}
//				}
//			}
//		}
		return true;
	}

	public ItemStack GetSuperiorElytra(Iterable<ItemStack> itemList)
	{
		if(itemList != null)
		{
			for (ItemStack item : itemList) 
			{
				if(item != null)
				{
					if(item.isEmpty())
					{
						continue;
					}
					if(item.getItem() != Items.ELYTRA)
					{
						continue;
					}
					NBTTagCompound nbt;
					if(item.hasTagCompound())
					{
						nbt = item.getTagCompound();
					}
					else 
					{
						nbt = new NBTTagCompound();
					}
					BoostingElytraTier elytraTier = null;
					float[] elytraStats = null;
					if(nbt.hasKey("randomTier"))
					{
						nbt.setBoolean("isSuperiorElytra", true);
						elytraTier = getRandomElytraTier();
						elytraStats = elytraTier.getRandomStats();
						item.setTagCompound(nbt);
					}
					if(nbt.hasKey("isLevelingElytra"))
					{
						nbt.setBoolean("isSuperiorElytra", true);
						if(!nbt.hasKey("elytraLevel"))
						{
							nbt.setInteger("elytraLevel", 1);
						}
						if(!nbt.hasKey("elytraCurrentXP"))
						{
							nbt.setDouble("elytraCurrentXP", 0d);
						}
						if(!nbt.hasKey("elytraRequiredXP"))
						{
							nbt.setInteger("elytraRequiredXP", (int) Math.pow(((double) (1) / EXPCONSTANT), 2));
						}
					}
					if(!nbt.hasKey("isSuperiorElytra"))
					{
						return null;
					}
					if(!nbt.hasKey("flightTime"))
					{
						if(elytraTier != null)
						{
							nbt.setFloat("flightTime", elytraStats[0]);	
						}
						else
						{
							nbt.setFloat("flightTime", 8F);							
						}
						item.setTagCompound(nbt);
					}
					if(!nbt.hasKey("flightAcceleration"))
					{
						if(elytraTier != null)
						{
							nbt.setFloat("flightAcceleration", elytraStats[1]);							
						}
						else
						{
							nbt.setFloat("flightAcceleration", 0.025F);
						}
						item.setTagCompound(nbt);
					}
					if(!nbt.hasKey("flightRecovery"))
					{
						if(elytraTier != null)
						{
							nbt.setFloat("flightRecovery", elytraStats[2]);								
						}
						else
						{
							nbt.setFloat("flightRecovery", 0.08F);	
						}
						item.setTagCompound(nbt);							
					}
					if(!nbt.hasKey("flightParticle"))
					{
						if(elytraTier != null)
						{
							nbt.setString("flightParticle", elytraTier.getRandomParticleEffect());	
						}
						else
						{
							nbt.setString("flightParticle", "fireworksSpark");	
						}
						item.setTagCompound(nbt);							
					}
					if(!nbt.hasKey("flightDuration"))
					{
						if(elytraTier != null)
						{
							nbt.setFloat("flightDuration", 0f);	
						}
						else
						{
							nbt.setFloat("flightDuration", 20F);	
						}
						item.setTagCompound(nbt);							
					}
					if(!nbt.hasKey("flightTier"))
					{
						if(elytraTier != null)
						{
							nbt.setString("flightTier", elytraTier.getTierName());	
						}
						else
						{
							nbt.setString("flightTier", "Custom");							
						}
						item.setTagCompound(nbt);							
					}
					if(!nbt.hasKey("partOffset"))
					{
						nbt.setDouble("partOffset", 1);	
						item.setTagCompound(nbt);							
					}
					if(!nbt.hasKey("partSpeed"))
					{
						nbt.setDouble("partSpeed", 0);	
						item.setTagCompound(nbt);							
					}
					if(!nbt.hasKey("partCount"))
					{
						nbt.setInteger("partCount", 5);	
						item.setTagCompound(nbt);							
					}
					if(!nbt.hasKey("depthDiver"))
					{
						if(elytraTier != null)
						{
							nbt.setBoolean("depthDiver", elytraTier.getRandomDepthDiver());	
						}
						else
						{
							nbt.setBoolean("depthDiver", false);								
						}
						item.setTagCompound(nbt);							
					}
					if(!nbt.hasKey("extraValue"))
					{
						nbt.setString("extraValue", "minecraft:air");	
						item.setTagCompound(nbt);							
					}
					if(elytraTier != null)
					{
						nbt.setBoolean("Unbreakable", true);								
						item.setTagCompound(nbt);
					}
					if(!nbt.hasKey("display"))
					{
						if(elytraTier != null)
						{
							nbt.setTag("display", new NBTTagCompound());
							nbt.getCompoundTag("display").setString("Name", elytraTier.getTierName() + " Elytra");	
							
						}
						else
						{
							nbt.setTag("display", new NBTTagCompound());
							nbt.getCompoundTag("display").setString("Name", TextFormatting.GOLD + "Boosting Elytra");							
						}
						item.setTagCompound(nbt);		
					}
					if(!nbt.getCompoundTag("display").hasKey("Lore"))
					{
						NBTTagList loreList = new NBTTagList();

						DecimalFormat df = new DecimalFormat("###.##");
						DecimalFormat df2 = new DecimalFormat("###.###");
						DecimalFormat df3 = new DecimalFormat("###.#");
						//if falling dust, block behind it
						if(!nbt.hasKey("isLevelingElytra"))
						{
							loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Tier: " + TextFormatting.GRAY + nbt.getString("flightTier")));							
						}
						else
						{
							loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Level: " + TextFormatting.GRAY + nbt.getInteger("elytraLevel")));
						}
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Flight Time: " + TextFormatting.GRAY + df.format(nbt.getFloat("flightTime"))));
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Flight Acceleration: " + TextFormatting.GRAY + df2.format(nbt.getFloat("flightAcceleration"))));
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Flight Recovery: " + TextFormatting.GRAY + df.format(nbt.getFloat("flightRecovery"))));
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Boost Particle: " + TextFormatting.GRAY + (!nbt.getString("extraValue").contains("minecraft:air") ? TextFormatting.GRAY + nbt.getString("extraValue") : nbt.getString("flightParticle"))));
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Particle Count: " + TextFormatting.GRAY + nbt.getInteger("partCount")));
						loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Depth Diver: " + TextFormatting.GRAY + nbt.getBoolean("depthDiver")));
						loreList.appendTag(new NBTTagString(TextFormatting.GRAY + "Hold Sneak to boost while flying"));
						loreList.appendTag(new NBTTagString(TextFormatting.GRAY + "Hold Sneak and jump while looking up to take off"));
						
						if(nbt.hasKey("isLevelingElytra"))
						{
							loreList.appendTag(new NBTTagString(TextFormatting.AQUA + "Exp Required: " + TextFormatting.GRAY + "(" + df3.format(nbt.getDouble("elytraCurrentXP")) + "/" + nbt.getInteger("elytraRequiredXP") + ")"));
							loreList.appendTag(new NBTTagString(Util.GetExpBarString((int) nbt.getDouble("elytraCurrentXP"), nbt.getInteger("elytraRequiredXP"))));
						}
						
						nbt.getCompoundTag("display").setTag("Lore", loreList);
						item.setTagCompound(nbt);
					}
					
					if(nbt.hasKey("isLevelingElytra"))
					{
						NBTTagList loreList = nbt.getCompoundTag("display").getTagList("Lore", Constants.NBT.TAG_STRING);
						if(loreList.getStringTagAt(loreList.tagCount() - 2).contains("Exp Required:"))
						{
							DecimalFormat df = new DecimalFormat("###.#");
							DecimalFormat df2 = new DecimalFormat("###.###");
							DecimalFormat df3 = new DecimalFormat("###.##");
							loreList.set(loreList.tagCount() - 2, new NBTTagString(TextFormatting.AQUA + "Exp Required: " + TextFormatting.GRAY + "(" + df.format(nbt.getDouble("elytraCurrentXP")) + "/" + nbt.getInteger("elytraRequiredXP") + ")"));
							loreList.set(loreList.tagCount() - 1, new NBTTagString(Util.GetExpBarString((int) nbt.getDouble("elytraCurrentXP"), nbt.getInteger("elytraRequiredXP"))));
							loreList.set(loreList.tagCount() - 11, new NBTTagString(TextFormatting.AQUA + "Level: " + TextFormatting.GRAY + nbt.getInteger("elytraLevel")));
							loreList.set(loreList.tagCount() - 10, new NBTTagString(TextFormatting.AQUA + "Flight Time: " + TextFormatting.GRAY + df.format(nbt.getFloat("flightTime"))));
							loreList.set(loreList.tagCount() - 9, new NBTTagString(TextFormatting.AQUA + "Flight Acceleration: " + TextFormatting.GRAY + df2.format(nbt.getFloat("flightAcceleration"))));
							loreList.set(loreList.tagCount() - 8, new NBTTagString(TextFormatting.AQUA + "Flight Recovery: " + TextFormatting.GRAY + df.format(nbt.getFloat("flightRecovery"))));
						}
					}
					
					return item;
				}
				
			}
		}
		return null;
	}
}

