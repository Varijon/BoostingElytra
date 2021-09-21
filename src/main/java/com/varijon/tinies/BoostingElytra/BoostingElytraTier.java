package com.varijon.tinies.BoostingElytra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class BoostingElytraTier 
{
	String tierName;
	int rarity;
	float minFlightTimeCap;
	float maxFlightTimeCap;
	float minFlightAccelerationCap;
	float maxFlightAccelerationCap;
	float minFlightRecoveryCap;
	float maxFlightRecoveryCap;
	int depthDiverChance;
	String[] particleOptions;
	
	
	public BoostingElytraTier(String tierName, int rarity, float minFlightTime, float maxFlightTime, float minFlightAccelerationCap, float maxFlightAccelerationCap, float minFlightRecoveryCap, float maxFlightRecoveryCap, int depthDiverChance, String[] particleOptions)
	{
		this.tierName = tierName;
		this.rarity = rarity;
		this.minFlightTimeCap = minFlightTime;
		this.maxFlightTimeCap = maxFlightTime;
		this.minFlightAccelerationCap = minFlightAccelerationCap;
		this.maxFlightAccelerationCap = maxFlightAccelerationCap;
		this.minFlightRecoveryCap = minFlightRecoveryCap;
		this.maxFlightRecoveryCap = maxFlightRecoveryCap;
		this.depthDiverChance = depthDiverChance;
		this.particleOptions = particleOptions;
	}
	//pick a random tier from list, rolling random for rarity number, until it falls within a rarity
	//upon applying a tier to a clean elytra, roll a number 0-100, then again for remainder, and again
	//these numbers are the percentage of the cap to give the elytra, if under minimum add minimum
	//eg: 60,20,10, could be 30ft, 0.02fa, 0.05fr


	
	//list of particles:
	//explode,fireworksSpark,splash,endRod,magicCrit,smoke,spell,instantSpell,witchMagic,dripWater,
	//dripLava,happyVillager,note,enchantmenttable,portal,flame,lava,reddust,heart,snowshovel,dragonbreath,damageIndicator
	//largesmoke
	
	//old tier numbers:
	//power: flighttime:2, flightacceleration:0.085,flightrecovery:0.05
	//balanced: flighttime:12, flightacceleration:0.038, flightrecovery:0.2
	//endurance: flighttime:40, flightacceleration:0.020, flightrecovery:0.1
	
	public float[] getRandomStats()
	{
		Random rng = new Random();
		int rng1 = rng.nextInt(80);
		if(rng1 < 10)
		{
			rng1+=10;
		}
		int rng2 = rng.nextInt(80- rng1);
		if(rng2 < 10)
		{
			rng2+=10;
		}
		int rng3 = 100 - rng1 - rng2;
		
//		System.out.println(rng1 + " - " + rng2 + " - " + rng3);
		
		ArrayList<Integer> shuffleList = new ArrayList<Integer>();
		shuffleList.add(rng1);
		shuffleList.add(rng2);
		shuffleList.add(rng3);
		Collections.shuffle(shuffleList);
		
		float randomAcceleration = maxFlightAccelerationCap / 100f * shuffleList.get(0);
		float randomFlightRecover = maxFlightRecoveryCap / 100f * shuffleList.get(1);
		float randomFlightTime = maxFlightTimeCap / 100f * shuffleList.get(2);
		
		if(randomAcceleration < minFlightAccelerationCap)
		{
			randomAcceleration += minFlightAccelerationCap;
		}
		if(randomFlightRecover < minFlightRecoveryCap)
		{
			randomFlightRecover += minFlightRecoveryCap;
		}
		if(randomFlightTime < minFlightTimeCap)
		{
			randomFlightTime += minFlightTimeCap;
		}
		
		return new float[]{randomFlightTime, randomAcceleration, randomFlightRecover};
	}
	
	public String getRandomParticleEffect()
	{
		Random rng = new Random();
		return particleOptions[rng.nextInt(particleOptions.length)];
	}
	
	public boolean getRandomDepthDiver()
	{
		Random rng = new Random();
		int randomNumber = rng.nextInt(101);
		if(depthDiverChance >= randomNumber)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public String getTierName() {
		return tierName;
	}


	public void setTierName(String tierName) {
		this.tierName = tierName;
	}


	public int getRarity() {
		return rarity;
	}


	public void setRarity(int rarity) {
		this.rarity = rarity;
	}


	public float getMinFlightTime() {
		return minFlightTimeCap;
	}


	public void setMinFlightTime(float minFlightTime) {
		this.minFlightTimeCap = minFlightTime;
	}


	public float getMaxFlightTime() {
		return maxFlightTimeCap;
	}


	public void setMaxFlightTime(float maxFlightTime) {
		this.maxFlightTimeCap = maxFlightTime;
	}


	public float getMinFlightAccelerationCap() {
		return minFlightAccelerationCap;
	}


	public void setMinFlightAccelerationCap(float minFlightAccelerationCap) {
		this.minFlightAccelerationCap = minFlightAccelerationCap;
	}


	public float getMaxFlightAccelerationCap() {
		return maxFlightAccelerationCap;
	}


	public void setMaxFlightAccelerationCap(float maxFlightAccelerationCap) {
		this.maxFlightAccelerationCap = maxFlightAccelerationCap;
	}


	public float getMinFlightRecoveryCap() {
		return minFlightRecoveryCap;
	}


	public void setMinFlightRecoveryCap(float minFlightRecoveryCap) {
		this.minFlightRecoveryCap = minFlightRecoveryCap;
	}


	public float getMaxFlightRecoveryCap() {
		return maxFlightRecoveryCap;
	}


	public void setMaxFlightRecoveryCap(float maxFlightRecoveryCap) {
		this.maxFlightRecoveryCap = maxFlightRecoveryCap;
	}


	public int getDepthDiverChance() {
		return depthDiverChance;
	}


	public void setDepthDiverChance(int depthDiverChance) {
		this.depthDiverChance = depthDiverChance;
	}


	public String[] getParticleOptions() {
		return particleOptions;
	}


	public void setParticleOptions(String[] particleOptions) {
		this.particleOptions = particleOptions;
	}
	
}
