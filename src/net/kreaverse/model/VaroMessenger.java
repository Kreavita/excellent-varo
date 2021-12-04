package net.kreaverse.model;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class VaroMessenger {

	private String excellentVaro = ChatColor.GRAY + "[" + ChatColor.BLUE + "Excellent" + ChatColor.DARK_AQUA + "V"
			+ ChatColor.AQUA + "A" + ChatColor.DARK_AQUA + "R" + ChatColor.AQUA + "O" + ChatColor.GRAY + "]"
			+ ChatColor.DARK_GRAY + " >> " + ChatColor.WHITE;

	public void playerTitle(UUID player, String top, ChatColor topColor, String bottom, ChatColor bottomColor) {
		playerTitle(Bukkit.getPlayer(player), top, topColor, bottom, bottomColor);
	}

	public void playerTitle(Player p, String top, ChatColor topC, String bottom, ChatColor bottomC) {
		if (p == null)
			return;
		p.showTitle(Title.title(Component.text(topC + top), Component.text(bottomC + bottom)));
	}

	public void playerMessage(Player p, String message, ChatColor color) {
		if (p == null)
			return;
		p.sendMessage(excellentVaro + color + message.replace("{name}", p.getName()));
	}

	public void playerMessage(UUID player, String msg, ChatColor color) {
		playerMessage(Bukkit.getPlayer(player), msg, color);
	}

	public void broadcast(String msg, ChatColor color) {
		Bukkit.getOnlinePlayers()
				.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1));
		Bukkit.broadcast(Component.text(excellentVaro + color + msg));
	}

	public void broadcastDeath(String victim, long aliveCount) {
		Bukkit.getOnlinePlayers()
				.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1));
		Bukkit.broadcast(Component.text(
				excellentVaro + ChatColor.RED + victim + " ist gestorben. Es leben noch " + aliveCount + " Spieler."));
	}

	public void broadcastKill(String attacker, String victim, long killCount, long aliveCount) {
		Bukkit.getOnlinePlayers()
				.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1));
		Bukkit.broadcast(Component.text(excellentVaro + ChatColor.RED + victim + " wurde von " + attacker
				+ " getötet. (" + killCount + ". Kill) Es leben noch " + aliveCount + " Spieler."));
	}

	public void errorMessage(@NotNull CommandSender sender, String errorMessage) {
		sender.sendMessage(excellentVaro + ChatColor.RED + "FEHLER: " + errorMessage);
	}

	public void successMessage(@NotNull CommandSender sender, String successMessage) {
		sender.sendMessage(excellentVaro + ChatColor.GREEN + successMessage);
	}

	public void playerError(@NotNull Player player, String errorMessage) {
		playerMessage(player, "FEHLER: " + errorMessage, ChatColor.RED);
	}

	public void playerSuccess(@NotNull Player player, String successMessage) {
		playerMessage(player, successMessage, ChatColor.GREEN);
	}

}
