package net.kreaverse.model;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class VaroConfig {

	private FileConfiguration configFile;
	public BukkitRunnable saveTask;

	public VaroConfig(@NotNull FileConfiguration config, @NotNull VaroGame game) {
		this.configFile = config;
		this.saveTask = new BukkitRunnable() {
			@Override
			public void run() {
				configFile.set("game.state", game.state.toInt);
				configFile.set("game.borderSize", game.borderSize);
				configFile.set("game.borderMinSize", game.borderMinSize);
				configFile.set("game.borderShrinkTime", game.borderShrinkTime);
				configFile.set("players", game.players);
			}
		};
	}
}
