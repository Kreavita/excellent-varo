package net.kreaverse.model;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class VaroConfig {
	
	private static @NotNull VaroConfig varoConfig = new VaroConfig();

	private VaroConfig() {
	}

	public static VaroConfig getInstance() {
		return varoConfig;
	}

	public FileConfiguration configFile;
	
	public void initialize(FileConfiguration config) {
		this.configFile = config;
	}
	
	public void save(VaroGame game) {
		//TODO: Save The Game State
	}
}
