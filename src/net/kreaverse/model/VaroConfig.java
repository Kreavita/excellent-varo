package net.kreaverse.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.ExcellentVARO;

public class VaroConfig {

	private FileConfiguration configFile;
	public BukkitRunnable saveTask;

	public VaroConfig(@NotNull ExcellentVARO plugin) {
		this.configFile = plugin.getConfig();
		this.saveTask = new BukkitRunnable() {
			@Override
			public void run() {
				configFile.set("game.state", plugin.getGame().getState().toInt);
				configFile.set("game.borderSize",
						(int) plugin.getServer().getWorlds().get(0).getWorldBorder().getSize());
				configFile.set("game.borderMinSize", plugin.getGame().borderMinSize);
				configFile.set("game.borderShrinkTime", plugin.getGame().borderShrinkTime);
				plugin.getGame().players.forEach(vp -> {
					configFile.set("players." + vp.player.toString() + ".teammate",
							(vp.getTeammate() == null) ? null : vp.getTeammate().toString());
					configFile.set("players." + vp.player.toString() + ".alive", vp.alive);
					for (String stat : vp.stats.keySet())
						configFile.set("players." + vp.player.toString() + ".stats." + stat, vp.stats.get(stat));
				});
			}
		};
		saveTask.runTaskTimer(plugin, 1200L, 1200L);
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

			UUID player = UUID.fromString(playerUUID);
			UUID teammate = (teammateUUID == null) ? null : UUID.fromString(teammateUUID);

			players.add(new VaroPlayer(player, teammate, configFile.getBoolean("players." + playerUUID + ".alive"),
					retrieveStats(player)));
		}

		return players;
	}
}
