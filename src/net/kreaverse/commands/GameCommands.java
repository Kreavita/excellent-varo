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
		if (!sender.isOp()) {
			msg.errorMessage(sender, "Dieser Befehl kann nur von Serveroperatoren ausgeführt werden.");
			return true;
		}

		Player p;
		VaroPlayer vp;

		switch (command.getName()) {

		case "start":
			if (game.state == GameState.ONGOING) {
				msg.errorMessage(sender, "Das Spiel läuft bereits.");
				return true;
			}
			if (args.length > 0) {
				try {
					game.start(Integer.valueOf(args[0]));
					return true;
				} catch (NumberFormatException e) {
					Bukkit.getLogger().log(Level.WARNING, "Start Command wurde mit ungültigem Integerwert aufgerufen!");
				}
			}
			game.start(61);
			return true;

		case "reset":
			if (game.state == GameState.IDLE) {
				msg.errorMessage(sender, "Das Spiel wurde noch nicht gestartet.");
				return true;
			}
			game.reset();
			return true;

		case "varokill":
			if (game.state != GameState.ONGOING) {
				msg.errorMessage(sender, "Es läuft gerade kein Spiel.");
				return true;
			}

			if (args.length != 1) {
				msg.errorMessage(sender, "Die Anzahl der Argumente stimmt nicht.");
				return false;
			}

			vp = game.getPlayerByUUID(Bukkit.getOfflinePlayer(args[0]).getUniqueId());

			if (!vp.alive) {
				msg.errorMessage(sender, "Dieser Spieler ist bereits tot.");
				return false;
			}

			vp.alive = false;
			p = Bukkit.getPlayer(args[0]);

			if (p != null) {
				game.updatePlayer(p);
			}

			return true;

		case "varorevive":
			if (game.state != GameState.ONGOING) {
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
				return false;
			}

			vp.alive = true;
			p = Bukkit.getPlayer(args[0]);

			if (p != null) {
				game.updatePlayer(p);
			}

			return true;

		default:
			return false;
		}
	}

}
