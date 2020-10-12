package DiscordBot;

public class ISCharacter {
	public int edge, iron, heart, shadow, wits;
	private int health, spirit, supply, xp, momentum;
	public String name;	
	
	public ISCharacter(String name) {
		this.name = name;
		this.xp = 0;
		
		this.edge = 0;
		this.iron = 0;
		this.heart = 0;
		this.shadow = 0;
		this.wits = 0;
		
		this.health = 5;
		this.spirit = 5;
		this.supply = 5;
		
		this.momentum = 2;
	}
	
	public int getHealth() { return this.health; }
	public void modHealth(int modValue) {
		health += modValue;
		if(health > 5) {health = 5;}
		if(health < 0) {health = 0;}
	}
	
	public int getSpirit() { return this.spirit; }
	public void modSpirit(int modValue) {
		spirit += modValue;
		if(spirit > 5) {spirit = 5;}
		if(spirit < 0) {spirit = 0;}
	}
	
	public int getSupply() { return this.supply; }
	public void modSupply(int modValue) {
		spirit += modValue;
		if(supply > 5) {supply = 5;}
		if(supply < 0) {supply = 0;}
	}
	
	// TODO: return that a level up occurred
	public int getXP() { return this.xp; }
	public void addXP(int value) {
		this.xp += value;
		if (this.xp >= 30) { this.xp = 0; } // Level up!
	}
	
	//TODO: add reset, burn, and max capabilities
	public int getMomentum() {return this.momentum;}
	public void modMomentum(int modValue) {
		momentum += modValue;
		if(momentum > 10) { momentum = 10;}
		if(momentum < -6) { momentum = -6;}
	}
	public void resetMomentum() { this.momentum = 2;}

}
