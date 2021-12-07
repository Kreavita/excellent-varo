package net.kreaverse.model;

import java.util.HashMap;
import java.util.UUID;

public class VaroPlayer {

	public UUID player;
	private UUID teammate;

	public boolean alive;
	public int revivesLeft;

	public HashMap<String, Double> stats;

	public UUID lastAttacker;
	public int attackedCooldown;

	public VaroPlayer(UUID p, int playerRevives) {
		player = p;
		teammate = null;

		alive = true;
		revivesLeft = playerRevives;

		lastAttacker = null;
		attackedCooldown = 0;

		stats = new HashMap<String, Double>();
		stats.put("kills", 0d);
		stats.put("deaths", 0d);
		stats.put("damageDealtToPlayers", 0d);
		stats.put("damageTakenFromPlayers", 0d);
		stats.put("attacksLanded", 0d);
		stats.put("attacksMissed", 0d);
		stats.put("shotsLanded", 0d);
		stats.put("shotsMissed", 0d);
	}

	public VaroPlayer(UUID p, UUID teammate, boolean alive, int revivesLeft, HashMap<String, Double> stats) {
		this(p, revivesLeft);
		this.teammate = teammate;
		this.alive = alive;
		this.revivesLeft = revivesLeft;
		stats.forEach((key, value) -> incrementStat(key, value));
	}

	public double incrementStat(String stat, double value) {
		if (!stats.containsKey(stat))
			stats.put(stat, 0D);

		double newValue = stats.get(stat) + value;
		stats.replace(stat, newValue);
		return newValue;
	}

	public void setTeammate(UUID teammate) {
		this.teammate = teammate;
	}

	public UUID getTeammate() {
		return this.teammate;
	}

	public void setAttacker(UUID attacker) {
		if (attacker.equals(player))
			return;
		lastAttacker = attacker;
		attackedCooldown = 31;
	}
}
