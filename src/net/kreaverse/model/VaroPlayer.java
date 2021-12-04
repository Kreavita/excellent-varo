package net.kreaverse.model;

import java.util.HashMap;
import java.util.UUID;

public class VaroPlayer {

	public UUID player;
	public UUID teammate;
	public boolean alive = true;

	public HashMap<String, Double> stats = new HashMap<String, Double>();
	public UUID lastAttacker = null;
	public int attackedCooldown = 0;

	public VaroPlayer(UUID p) {
		player = p;
		stats.put("kills", 0d);
		stats.put("damageDealt", 0d);
		stats.put("damageTaken", 0d);
		stats.put("attacksLanded", 0d);
		stats.put("attacksMissed", 0d);
		stats.put("shotsLanded", 0d);
		stats.put("shotsMissed", 0d);
	}

	public double incrementStat(String stat, double value) {
		if (!stats.containsKey(stat))
			stats.put(stat, 0D);

		double newValue = stats.get(stat) + value;
		stats.replace(stat, newValue);
		return newValue;
	}

	public void attacked(UUID attacker) {
		lastAttacker = attacker;
		attackedCooldown = 31;
	}
}
