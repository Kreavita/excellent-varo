package net.kreaverse;

import java.util.logging.Level;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.kreaverse.commands.GameCommands;
import net.kreaverse.commands.StatsCommand;
import net.kreaverse.commands.TeamCommands;
import net.kreaverse.listeners.BlockChangesListener;
import net.kreaverse.listeners.EntityDamageListener;
import net.kreaverse.listeners.GameProgressionListener;
import net.kreaverse.listeners.PlayerInteractListener;
import net.kreaverse.listeners.PlayerServerListener;
import net.kreaverse.model.VaroConfig;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;

public class ExcellentVARO extends JavaPlugin {

	private VaroGame game;
	private VaroConfig config;
	private VaroMessenger msg;

	private PluginManager pm = this.getServer().getPluginManager();

	public void onEnable() {
		saveDefaultConfig();
		msg = new VaroMessenger();
		game = new VaroGame(this, msg);
		config = new VaroConfig(this.getConfig(), game);

		pm.registerEvents(new PlayerServerListener(game, msg), this);
		pm.registerEvents(new PlayerInteractListener(game, msg), this);
		pm.registerEvents(new EntityDamageListener(game, msg), this);
		pm.registerEvents(new BlockChangesListener(game), this);
		pm.registerEvents(new GameProgressionListener(game), this);

		CommandExecutor teamExecutor = new TeamCommands(game, msg);
		CommandExecutor gameExecutor = new GameCommands(game, msg);

		getCommand("stats").setExecutor(new StatsCommand(game, msg));

		getCommand("team").setExecutor(teamExecutor);
		getCommand("unteam").setExecutor(teamExecutor);

		getCommand("start").setExecutor(gameExecutor);
		getCommand("reset").setExecutor(gameExecutor);
		getCommand("varokill").setExecutor(gameExecutor);
		getCommand("varorevive").setExecutor(gameExecutor);

		getLogger().log(Level.INFO, "VARO Plugin has loaded");
	}

	public void onDisable() {
		config.saveTask.run();
		saveConfig();
	}
	
	public VaroGame getGame() {
		return game;
	}
}
