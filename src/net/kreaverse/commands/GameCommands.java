package net.kreaverse.commands;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;

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
		
		int counter;

		if (command.getName().strip().equalsIgnoreCase("pause")) {
			if (game.getState() != GameState.ONGOING) {
				msg.errorMessage(sender, "Es läuft gerade kein Spiel.");
				return true;
			}
			if (game.paused) {
				msg.errorMessage(sender, "Das Spiel ist bereits pausiert.");
				return true;
			}
			counter = 2;
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
		return false;
	}

}
