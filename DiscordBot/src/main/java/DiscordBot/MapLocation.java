package DiscordBot;

import java.awt.geom.AffineTransform;

public class MapLocation {
	public Hex hex;
	public AffineTransform transform;
	
	public MapLocation(Hex hex, AffineTransform transform ) {
		this.hex = hex;
		this.transform = transform;
	}
}

