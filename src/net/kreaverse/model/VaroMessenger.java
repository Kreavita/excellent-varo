package net.kreaverse.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class VaroMessenger {

	private String formattedTime() {
		return ChatColor.GRAY + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " | "
				+ ChatColor.WHITE;
	}

	private String formattedPrefix() {
		return formattedTime() + ChatColor.GRAY + "[" + ChatColor.BLUE + "Excellent" + ChatColor.DARK_AQUA + "V"
				+ ChatColor.AQUA + "A" + ChatColor.DARK_AQUA + "R" + ChatColor.AQUA + "O" + ChatColor.GRAY + "]"
				+ ChatColor.DARK_GRAY + " >> " + ChatColor.WHITE;
	}

	public void playerTitle(UUID player, String top, ChatColor topColor, String bottom, ChatColor bottomColor) {
		playerTitle(Bukkit.getPlayer(player), top, topColor, bottom, bottomColor);
	}

	public void playerTitle(Player p, String top, ChatColor topC, String bottom, ChatColor bottomC) {
		if (p == null)
			return;
		p.showTitle(Title.title(Component.text(topC + top), Component.text(bottomC + bottom)));
	}

	public void chatMessage(Player sender, Component msg) {
		Bukkit.broadcast(Component.text(formattedTime() + ChatColor.GREEN + "SPIELER " + ChatColor.GOLD
				+ sender.getName() + ChatColor.DARK_GRAY + " >> " + ChatColor.WHITE).append(msg));
	}

	public void spectatorMessage(Player sender, Player otherSpectator, Component msg) {
		otherSpectator
				.sendMessage(
						Component
								.text(formattedTime() + ChatColor.GRAY + "ZUSCHAUER " + ChatColor.LIGHT_PURPLE
										+ sender.getName() + ChatColor.DARK_GRAY + " >> " + ChatColor.GRAY)
								.append(msg));
	}

	public void pauseTitle(Player p, int minutes) {
		if (p == null)
			return;
		p.showTitle(Title.title(Component.text(ChatColor.GOLD + "Spiel pausiert"),
				Component.text(ChatColor.YELLOW + "Das Spiel wird in " + minutes + " Minuten fortgesetzt."),
				Times.of(Duration.ZERO, Duration.ofMinutes(minutes), Duration.ZERO)));
	}

	public void resumeTitle(Player p) {
		playerTitle(p, "Spiel fortgesetzt", ChatColor.GREEN, "Das Spiel geht weiter!", ChatColor.AQUA);
	}

	public void playerMessage(Player p, String message, ChatColor color) {
		if (p == null)
			return;
		p.sendMessage(formattedPrefix() + color + message.replace("{name}", p.getName()));
	}

	public void playerMessage(UUID player, String msg, ChatColor color) {
		playerMessage(Bukkit.getPlayer(player), msg, color);
	}

	public void broadcast(String msg, ChatColor color) {
		Bukkit.getOnlinePlayers()
				.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1));

		Bukkit.broadcast(Component.text(formattedPrefix() + color + msg));
	}

	public void broadcastDeathMessage(Component message, long aliveCount) {
		Bukkit.getOnlinePlayers()
				.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1));

		Bukkit.broadcast(Component.text(formattedPrefix()).append(message.color(NamedTextColor.RED))
				.append(Component.text(". Es leben noch " + (aliveCount - 1) + " Spieler.").color(NamedTextColor.RED)));
	}

	public void broadcastKillMessage(String attacker, Component message, long killCount, long aliveCount) {
		Bukkit.getOnlinePlayers()
				.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1));

		Bukkit.broadcast(Component.text(formattedPrefix()).append(message.color(NamedTextColor.RED))
				.append(Component.text(". (" + killCount + ". Kill) Es leben noch " + (aliveCount - 1) + " Spieler.")
						.color(NamedTextColor.RED)));
	}

	public void broadcastRevive(String victim, long aliveCount) {
		Bukkit.getOnlinePlayers()
				.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1));

		Bukkit.broadcast(Component.text(formattedPrefix() + ChatColor.LIGHT_PURPLE + victim
				+ " wude wiederbelebt. Es leben jetzt wieder " + (aliveCount + 1) + " Spieler."));
	}

	public void errorMessage(@NotNull CommandSender sender, String errorMessage) {
		sender.sendMessage(formattedPrefix() + ChatColor.RED + "FEHLER: " + errorMessage);
	}

	public void successMessage(@NotNull CommandSender sender, String successMessage) {
		sender.sendMessage(formattedPrefix() + ChatColor.GREEN + successMessage);
	}

	public void playerError(@NotNull Player player, String errorMessage) {
		playerMessage(player, " FEHLER: " + errorMessage, ChatColor.RED);
	}

	public void playerSuccess(@NotNull Player player, String successMessage) {
		playerMessage(player, successMessage, ChatColor.GREEN);
	}

}
