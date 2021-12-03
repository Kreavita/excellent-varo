package net.kreaverse.model;

import java.util.HashMap;
import java.util.UUID;

public class VaroPlayer {

	public UUID player;
	public UUID teammateUUID;
	public boolean alive = true;

	public HashMap<String, Double> stats = new HashMap<String, Double>();
	public long lastAttacked;

	public VaroPlayer(UUID p) {
		player = p;
	}

	public double incrementStat(String stat, double value) {
		if (!stats.containsKey(stat))
			stats.put(stat, 0D);

		double newValue = stats.get(stat) + value;
		stats.replace(stat, newValue);
		return newValue;
	}
}
