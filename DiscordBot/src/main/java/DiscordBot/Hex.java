package DiscordBot;
public class Hex {
	public int elevation;
	public boolean blocksLOS;
	public int defence;
	public boolean isObjective;
	public boolean isStartSpace;
	public boolean isEnemyStartSpace;
	public int x; 
	public int y;
	
	public Hex(int elevation, int defence, boolean blocksLOS, boolean isObjective, boolean isStartSpace) {
		this.elevation = elevation;
		this.defence = defence;
		this.blocksLOS = blocksLOS;
		this.isObjective = isObjective;
		this.isStartSpace = isStartSpace;
		this.x = this.y = 0;
	}
	
	public Hex() {
		this.elevation = 0;
		this.defence = 3;
		this.blocksLOS = false;
		this.isObjective = false;
		this.isStartSpace = false;
		this.x = this.y = 0;
	}
}
