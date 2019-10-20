package DiscordBot;

import java.text.AttributedString;

public class GeneratedMission {
	
	public static enum MISSION_TYPE {
		DESTROY_INFRASTRUCTURE, DEFEND, PERIMETER_CHECK, HIT_AND_RUN, RESCUE, SCOUT_AND_CLEAR, SECURE_AND_HOLD, DOWNLINK
	}

	public Hex[][] map;
	public MISSION_TYPE mission;
	public int requiredGroups;	
	public AttributedString overview;
	public AttributedString primary;
	public AttributedString secondary;
	public AttributedString setup;
}
