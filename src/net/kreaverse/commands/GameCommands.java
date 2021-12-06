package net.kreaverse.commands;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;

public class GameCommands implements CommandExecutor {
	private VaroGame game;
	private VaroMessenger msg;

	public GameCommands(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		Player p;
		VaroPlayer vp;
		int counter;

		if (command.getName().strip().equalsIgnoreCase("pause")) {
			System.out.println(command.getName());
			if (game.getState() != GameState.ONGOING) {
				msg.errorMessage(sender, "Es läuft gerade kein Spiel.");
				return true;
			}
			if (game.paused) {
				msg.errorMessage(sender, "Das Spiel ist bereits pausiert.");
				return true;
			}
			counter = 15;
			if (args.length > 0) {
				try {
					counter = Integer.valueOf(args[0]);
				} catch (NumberFormatException e) {
					Bukkit.getLogger().log(Level.WARNING, "Pause Command wurde mit ungültigem Integerwert aufgerufen!");
				}
			}
			game.pause(counter, sender.getName());
			return true;
		}
		if (command.getName().strip().equalsIgnoreCase("unpause")) {
			System.out.println(command.getName());
			if (game.getState() != GameState.ONGOING) {
				msg.errorMessage(sender, "Es läuft gerade kein Spiel.");
				return true;
			}
			if (!game.paused) {
				msg.errorMessage(sender, "Das Spiel ist nicht pausiert.");
				return true;
			}
			game.resume(sender.getName());
			return true;
		}

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

			p = Bukkit.getPlayer(args[0]);
			if (p != null) {
				game.playerKill(p);
			} else {
				vp.alive = false;
			}
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
			if (p != null) {
				game.playerRevive(p);
			}

			return true;

		default:
			return false;
		}
	}

}
