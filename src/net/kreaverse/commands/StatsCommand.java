package net.kreaverse.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;

public class StatsCommand implements CommandExecutor {
	private VaroGame game;
	private VaroMessenger msg;

	public StatsCommand(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {

		OfflinePlayer target;

		if (args.length < 1) {
			if (!(sender instanceof Player)) {
				msg.errorMessage(sender, "Die Konsole hat keine Stats. Bitte gib einen Spielernamen mit an.");
				return false;
			}
			target = (Player) sender;
		} else {
			target = Bukkit.getOfflinePlayer(args[0]);
		}

		if (target == null) {
			msg.errorMessage(sender, "Der Spieler wurde nicht gefunden.");
			return true;
		}

		VaroPlayer vp = game.getPlayerByUUID(target.getUniqueId());

		if (vp == null) {
			msg.errorMessage(sender, "Der Spieler hat noch kein Varo gespielt.");
			return true;
		}

		msg.successMessage(sender, "Statistiken für " + target.getName());
		vp.stats.forEach((statName, statValue) -> {
			msg.successMessage(sender, "\t" + statName + " : " + (float) Math.round(statValue * 100) / 100f);
		});

		return true;

	}

}
