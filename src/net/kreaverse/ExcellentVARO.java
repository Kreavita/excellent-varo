package net.kreaverse;

import java.util.logging.Level;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.kreaverse.commands.GameCommands;
import net.kreaverse.commands.OperatorCommands;
import net.kreaverse.commands.StatsCommand;
import net.kreaverse.commands.TeamCommands;
import net.kreaverse.listeners.BlockChangesListener;
import net.kreaverse.listeners.EntityDamageListener;
import net.kreaverse.listeners.GameProgressionListener;
import net.kreaverse.listeners.PlayerCraftListener;
import net.kreaverse.listeners.PlayerInteractListener;
import net.kreaverse.listeners.PlayerServerListener;
import net.kreaverse.model.VaroConfig;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;

public class ExcellentVARO extends JavaPlugin {

	private VaroGame game;
	private VaroConfig cfg;

	private PluginManager pm = this.getServer().getPluginManager();

	public void onEnable() {
		saveDefaultConfig();
		VaroMessenger msg = new VaroMessenger();
		cfg = new VaroConfig(this);
		game = new VaroGame(this, msg, cfg);

		pm.registerEvents(new PlayerCraftListener(game, msg, this), this);
		
		pm.registerEvents(new PlayerServerListener(game, msg), this);
		pm.registerEvents(new EntityDamageListener(game, msg), this);

		pm.registerEvents(new PlayerInteractListener(game), this);
		pm.registerEvents(new BlockChangesListener(game), this);
		pm.registerEvents(new GameProgressionListener(game), this);

		getCommand("stats").setExecutor(new StatsCommand(game, msg));

		CommandExecutor teamExecutor = new TeamCommands(game, msg);
		getCommand("team").setExecutor(teamExecutor);
		getCommand("unteam").setExecutor(teamExecutor);

		CommandExecutor gameExecutor = new GameCommands(game, msg);
		getCommand("pause").setExecutor(gameExecutor);
		getCommand("unpause").setExecutor(gameExecutor);

		CommandExecutor opExecutor = new OperatorCommands(game, msg, this);
		getCommand("start").setExecutor(opExecutor);
		getCommand("reset").setExecutor(opExecutor);
		getCommand("varokill").setExecutor(opExecutor);
		getCommand("varorevive").setExecutor(opExecutor);
		getCommand("saveconfig").setExecutor(opExecutor);

		getLogger().log(Level.INFO, "VARO Plugin has loaded");
	}

	public void onDisable() {
		saveConfig();
	}

	public void saveConfig() {
		cfg.saveTask.run();
		super.saveConfig();
	}

	public VaroGame getGame() {
		return game;
	}
}
