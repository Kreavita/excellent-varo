package net.kreaverse.commands;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.ExcellentVARO;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;

public class OperatorCommands implements CommandExecutor {
	private VaroGame game;
	private VaroMessenger msg;
	private ExcellentVARO plugin;

	public OperatorCommands(VaroGame game, VaroMessenger msg, ExcellentVARO plugin) {
		this.game = game;
		this.msg = msg;
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		Player p;
		VaroPlayer vp;
		int counter;

		if (!sender.isOp()) {
			msg.errorMessage(sender, "Dieser Befehl kann nur von Serveroperatoren ausgeführt werden.");
			return true;
		}

		switch (command.getName()) {
		case "start":
			if (game.getState() == GameState.ONGOING) {
				msg.errorMessage(sender, "Das Spiel läuft bereits.");
				return true;
			}
			counter = 61;
			if (args.length > 0) {
				try {
					counter = Integer.valueOf(args[0]);
				} catch (NumberFormatException e) {
					Bukkit.getLogger().log(Level.WARNING, "Start Command wurde mit ungültigem Integerwert aufgerufen!");
				}
			}
			game.start(counter);
			return true;

		case "reset":
			if (game.getState() == GameState.IDLE) {
				msg.errorMessage(sender, "Das Spiel wurde noch nicht gestartet.");
				return true;
			}
			game.updateState(GameState.IDLE);
			msg.broadcast("Das Spiel wurde zurückgesetzt!", ChatColor.YELLOW);
			return true;

		case "varokill":
			if (args.length != 1) {
				msg.errorMessage(sender, "Die Anzahl der Argumente stimmt nicht.");
				return false;
			}

			if (game.getState() != GameState.ONGOING) {
				msg.errorMessage(sender, "Es läuft gerade kein Spiel.");
				return true;
			}

			vp = game.getPlayerByUUID(Bukkit.getOfflinePlayer(args[0].strip()).getUniqueId());
			if (vp == null || !vp.alive) {
				msg.errorMessage(sender, "Dieser Spieler existiert nicht oder ist bereits tot.");
				return true;
			}

			msg.broadcastDeath(Bukkit.getOfflinePlayer(args[0].strip()).getName(), game.aliveCount);

			p = Bukkit.getPlayer(args[0]);
			if (p != null) {
				game.playerKill(p);
			} else {
				game.playerKill(vp);
			}
			msg.successMessage(sender, p.getName() + " wurde getötet.");
			return true;

		case "varorevive":
			if (game.getState() != GameState.ONGOING) {
				msg.errorMessage(sender, "Es läuft gerade kein Spiel.");
				return true;
			}

			if (args.length != 1) {
				msg.errorMessage(sender, "Die Anzahl der Argumente stimmt nicht.");
				return false;
			}

			vp = game.getPlayerByUUID(Bukkit.getOfflinePlayer(args[0]).getUniqueId());
			if (vp.alive) {
				msg.errorMessage(sender, "Dieser Spieler ist bereits am Leben.");
				return true;
			}

			p = Bukkit.getPlayer(args[0]);
			msg.broadcastRevive(Bukkit.getOfflinePlayer(args[0].strip()).getName(), game.aliveCount);
			if (p != null) {
				game.playerRevive(p);
			} else {
				game.playerRevive(vp);
			}

			msg.successMessage(sender, p.getName() + " wurde wiederbelebt.");
			return true;

		case "saveconfig":
			plugin.saveConfig();
			msg.successMessage(sender, "Das Spiel wurde gespeichert.");
			return true;
			
		default:
			return false;
		}
	}

}
