package net.kreaverse.commands;

import java.util.HashMap;
import java.util.UUID;

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

public class TeamCommands implements CommandExecutor {
	private VaroGame game;
	private VaroMessenger msg;

	private HashMap<UUID, UUID> teamRequests;

	public TeamCommands(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
		teamRequests = new HashMap<UUID, UUID>();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if (!(sender instanceof Player)) {
			msg.errorMessage(sender, "Dieser Befehl kann nur von Spielern ausgeführt werden");
			return false;
		}

		Player p = (Player) sender;
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());

		if (vp == null) {
			msg.errorMessage(p, "Du bist kein Mitspieler.");
			return true;
		}

		switch (command.getName()) {
		case "team":
			if (game.getState() != GameState.IDLE) {
				msg.errorMessage(p, "Du kannst kein neues Teammitglied festlegen, das Spiel hat bereits begonnen.");
				return true;
			}

			if (vp.getTeammate() != null) {
				msg.errorMessage(p, "Du bist bereits in einem Team, steige aus deinem Team aus mit '/unteam'.");
				return true;
			}

			if (args.length == 0) {
				msg.errorMessage(p, "Du musst einen Spielernamen angeben");
				return false;
			}

			Player recipient = Bukkit.getPlayer(args[0]);

			if (recipient == null) {
				msg.errorMessage(p, "Der Spieler ist nicht online und kann deine Anfrage nicht annehmen.");
				return true;
			}

			VaroPlayer vpRecipient = game.getPlayerByUUID(recipient.getUniqueId());

			if (vpRecipient == null) {
				msg.errorMessage(p, "Der Spieler wurde nicht gefunden.");
				return true;
			}

			if (vpRecipient.getTeammate() != null) {
				msg.errorMessage(p,
						"Der Spieler ist bereits in einem Team, bitte ihn aus seinem Team auszusteigen mit '/unteam'.");
				return true;
			}

			if (teamRequests.get(recipient.getUniqueId()) != null
					&& teamRequests.get(recipient.getUniqueId()).equals(p.getUniqueId())) {
				vpRecipient.setTeammate(vp.player);
				vp.setTeammate(vpRecipient.player);

				game.updateTeamGlow(p, recipient);

				msg.successMessage(p, "Teamanfrage von " + recipient.getName() + " angenommen!");
				msg.successMessage(recipient, p.getName() + " hat deine Teamanfrage angenommen!");
				teamRequests.remove(recipient.getUniqueId());
				teamRequests.remove(p.getUniqueId());
				return true;
			}

			if (teamRequests.get(p.getUniqueId()) != null
					&& teamRequests.get(p.getUniqueId()).equals(recipient.getUniqueId())) {
				msg.errorMessage(p, "Du hast " + recipient.getName() + " schon eine Teamanfrage geschickt");
				return true;
			}

			msg.successMessage(p, recipient.getName() + " wurde eine Teamanfrage geschickt!");
			msg.successMessage(recipient,
					p.getName() + " hat dir eine Teamanfrage geschickt! Nimm sie an mit '/team " + p.getName() + "'!");
			teamRequests.put(p.getUniqueId(), recipient.getUniqueId());

			break;
		case "unteam":
			if (vp.getTeammate() == null) {
				msg.errorMessage(p, "Du hast kein Teammitglied!");
				return true;
			}

			VaroPlayer oldTeammate = game.getPlayerByUUID(vp.getTeammate());

			if (oldTeammate != null) {
				oldTeammate.setTeammate(null);
				if (Bukkit.getPlayer(oldTeammate.player) != null) {
					msg.successMessage(Bukkit.getPlayer(oldTeammate.player),
							"Dein Team mit " + p.getName() + "entfernt!");
				}
			}
			vp.setTeammate(null);
			msg.successMessage(p, "Dein Team wurde aufgelöst!!");
			game.updateTeamGlow(p, Bukkit.getPlayer(oldTeammate.player));

			break;

		default:
			return false;
		}

		return true;
	}

}
