package net.kreaverse.model;

import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class VaroMessenger {
	private static @NotNull VaroMessenger varoMessenger = new VaroMessenger();

	private VaroMessenger() {
	}

	public static VaroMessenger getInstance() {
		return varoMessenger;
	}

	private Server server;
	private String excellentVaro = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "Excellent" + ChatColor.DARK_RED
			+ "V" + ChatColor.RED + "A" + ChatColor.DARK_RED + "R" + ChatColor.RED + "O" + ChatColor.DARK_GRAY + "]"
			+ ChatColor.DARK_GRAY + ">>" + ChatColor.WHITE;

	public void initialize(@NotNull Server server) {
		this.server = server;
	}

	public void msgPlayer(UUID player, String msg) {
		this.server.getPlayer(player).sendMessage("[ExcellentVARO] >> " + msg);
	}

	public void broadcast(String msg) {
		this.server.broadcast(excellentVaro + msg, Server.BROADCAST_CHANNEL_USERS);
	}

	public void deathMsg(Player victim, long aliveCount) {
		this.server.broadcast(excellentVaro + ChatColor.RED + victim.getName() + " ist gestorben. Es leben noch "
				+ aliveCount + " Spieler.", Server.BROADCAST_CHANNEL_USERS);

	}

	public void killMsg(Player attacker, Player victim, long killCount, long aliveCount) {
		this.server.broadcast(
				excellentVaro + ChatColor.RED + victim.getName() + " wurde von " + attacker.getName() + " getötet. ("
						+ killCount + ". Kill) Es leben noch " + aliveCount + " Spieler.",
				Server.BROADCAST_CHANNEL_USERS);

	}

	public void playerError(@NotNull Player player, String errorMessage) {
		player.sendMessage(excellentVaro + ChatColor.RED + errorMessage);
	}
}
