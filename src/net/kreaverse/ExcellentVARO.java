package net.kreaverse;

import java.util.logging.Level;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.kreaverse.listeners.EntityDamageByEntityListener;
import net.kreaverse.listeners.PlayerJoinListener;
import net.kreaverse.listeners.PlayerMessageListener;
import net.kreaverse.listeners.PlayerQuitListener;
import net.kreaverse.model.VaroConfig;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;

public class ExcellentVARO extends JavaPlugin {

	private VaroGame game = VaroGame.getInstance();
	private VaroConfig config = VaroConfig.getInstance();
	private VaroMessenger messenger = VaroMessenger.getInstance();
	
	private PluginManager pm = this.getServer().getPluginManager();

	public void onEnable() {
		this.saveDefaultConfig();		
		config.initialize(this.getConfig());
		messenger.initialize(this.getServer());
		game.restore();

		pm.registerEvents(new PlayerJoinListener(), this);
		pm.registerEvents(new PlayerQuitListener(), this);
		pm.registerEvents(new PlayerMessageListener(), this);
		pm.registerEvents(new EntityDamageByEntityListener(), this);
		this.getLogger().log(Level.INFO, "VARO Plugin has loaded");
	}

	public void onDisable() {
		this.saveConfig();
	}
}
