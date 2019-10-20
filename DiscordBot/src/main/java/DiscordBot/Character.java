package DiscordBot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONObject;

public class Character {
	int strength;
	int dexterity;
	int constitution;
	int intelligence;
	int wisdom;
	int charisma;
	
	int level;
	String name;
	String background;
	String backgroundDesc;
	
	Map<String, Integer> skills;
	
	public Character(String name, JSONObject RPGData) {
		this.strength = rollStat();
		this.dexterity = rollStat();
		this.constitution = rollStat();
		this.intelligence = rollStat();
		this.wisdom = rollStat();
		this.charisma = rollStat();
		
		this.name = name;
		this.level = 0;
		
		JSONObject rgpData = RPGData.getJSONArray("Background").getJSONObject(ThreadLocalRandom.current().nextInt(RPGData.getJSONArray("Background").length()));
		this.background = rgpData.getString("Title");
		this.backgroundDesc = rgpData.getString("Description");		
		skills = new HashMap<String, Integer>();
		String[] skillList = rgpData.getString("QuickSkills").split(",");
		for(String skl : skillList) {
			skills.put(skl, 0);
		}
	}
	
	public int D6() {
		return ThreadLocalRandom.current().nextInt(6)+1;
	}
	
	public int getModifier(int statScore) {
		if(statScore >= 18) { return 2; }
		if(statScore >= 14) { return 1; }
		if(statScore >= 8) { return 0; }
		if(statScore >= 4) { return -1;}
		return -2;
	}
	
	public int D6(int numDice) {
		int total = 0;
		for(int i = 0; i < numDice; i++)
		{
			total += D6();
		}
		return total;
	}
	
	public int rollStat() {
		List<Integer> rolls = new ArrayList<Integer>();
		for(int i = 0; i < 4; i++) {
			rolls.add(D6());
		}
		Collections.sort(rolls); 
		Collections.reverse(rolls);
		rolls.remove(3);
		return rolls.get(0) + rolls.get(1) + rolls.get(2);
	}
}
