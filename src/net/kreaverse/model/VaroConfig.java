package net.kreaverse.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.ExcellentVARO;

public class VaroConfig {

	private FileConfiguration configFile;
	public BukkitRunnable saveTask;
	private int saveInterval;

	public VaroConfig(@NotNull ExcellentVARO plugin) {
		configFile = plugin.getConfig();
		saveInterval = configFile.getInt("defaults.saveEveryMinutes");
		saveTask = new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.getLogger().log(Level.INFO, "Saving Configuration and Data...");
				configFile.set("game.state", plugin.getGame().getState().toInt);
				configFile.set("game.borderSize",
						(int) plugin.getServer().getWorlds().get(0).getWorldBorder().getSize());
				plugin.getGame().players.forEach(vp -> {
					configFile.set("players." + vp.player.toString() + ".teammate",
							(vp.getTeammate() == null) ? null : vp.getTeammate().toString());
					configFile.set("players." + vp.player.toString() + ".alive", vp.alive);
					configFile.set("players." + vp.player.toString() + ".revivesLeft", vp.revivesLeft);
					for (String stat : vp.stats.keySet())
						configFile.set("players." + vp.player.toString() + ".stats." + stat, vp.stats.get(stat));
				});
			}
		};
		saveTask.runTaskTimer(plugin, 20L * 60 * saveInterval, 20L * 60 * saveInterval);
	}

	private HashMap<String, Double> retrieveStats(UUID uuid) {
		HashMap<String, Double> stats = new HashMap<String, Double>();
		for (String key : configFile.getConfigurationSection("players." + uuid.toString() + ".stats").getKeys(false))
			stats.put(key, configFile.getDouble("players." + uuid.toString() + ".stats." + key));
		return stats;
	}

	public ArrayList<VaroPlayer> loadPlayers() {
		ArrayList<VaroPlayer> players = new ArrayList<VaroPlayer>();

		if (configFile.getConfigurationSection("players") == null)
			return players;

		for (String playerUUID : configFile.getConfigurationSection("players").getKeys(false)) {

			String teammateUUID = configFile.getString("players." + playerUUID + ".teammate");
			int revivesLeft = configFile.getInt("players." + playerUUID + ".revivesLeft");
			boolean alive = configFile.getBoolean("players." + playerUUID + ".alive");

			UUID player = UUID.fromString(playerUUID);
			UUID teammate = (teammateUUID == null) ? null : UUID.fromString(teammateUUID);

			players.add(new VaroPlayer(player, teammate, alive, revivesLeft, retrieveStats(player)));
		}

		return players;
	}
}
