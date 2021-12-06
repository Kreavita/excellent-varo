package net.kreaverse.tasks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroPlayer;
import net.kyori.adventure.text.Component;

public class ScoreboardUpdater extends BukkitRunnable {
	private VaroGame game;

	public ScoreboardUpdater(VaroGame game) {
		this.game = game;
	}

	@Override
	public void run() {
		game.players.stream().filter(vp -> Bukkit.getPlayer(vp.player) != null).forEach(vp -> updateScoreboard(vp));
//		Bukkit.getOnlinePlayers().forEach(p ->game.pl.sendUpdatePackets(p));
	}

	public void updateScoreboard(@NotNull VaroPlayer vp) {
		Player p = Bukkit.getPlayer(vp.player);
		if (p != null)
			updateScoreboard(p, vp, 0, 0);
	}

	@SuppressWarnings("unused")
	private void updateScoreboard(@NotNull Player p) {
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());
		if (vp != null)
			updateScoreboard(p, vp, 0, 0);
	}

	private void updateScoreboard(@NotNull Player p, @NotNull VaroPlayer vp, double pDamage, double tmDamage) {
		UUID tm = vp.getTeammate();

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective varo = scoreboard.registerNewObjective("varo", "Be the last team alive!",
				Component.text("ExcellentVARO"));

		varo.displayName(Component.text("§u§l§7[§9Excellent§3V§bA§3R§bO§7]"));
		varo.setDisplaySlot(DisplaySlot.SIDEBAR);

		varo.getScore("     ").setScore(100);
		String stateStr;
		switch (game.getState()) {
		case FINISHED:
			stateStr = "§2§lBEENDET";
			break;
		case ONGOING:
			stateStr = "§c§lLAUFENDES SPIEL";
			break;
		default:
			stateStr = "§7§lVorbereitung";
			break;
		}

		varo.getScore(" Spielstatus: " + stateStr).setScore(99);
		varo.getScore(" Lebende Spieler: §c§l" + game.aliveCount).setScore(98);
		varo.getScore(
				" Border: §c§l" + (int) Bukkit.getServer().getWorlds().get(0).getWorldBorder().getSize() + " Blöcke")
				.setScore(97);
		varo.getScore("    ").setScore(90);
		varo.getScore(" §n§lDein Team:").setScore(89);
		varo.getScore("   ").setScore(80);

		varo.getScore(" §l§u§g" + p.getName() + ": "
				+ ((vp.alive) ? "§a§l" + Math.max(0, Math.round(p.getHealth() - pDamage) / 2f) + " HP" : "§c§lTOT"))
				.setScore(79);
		varo.getScore(" Kills: §2§l" + Math.round(vp.stats.get("kills"))).setScore(78);
		varo.getScore(" Leben: §a§l" + 1).setScore(77);
		varo.getScore("  ").setScore(70);

		if (tm != null) {
			VaroPlayer vpTeammate = game.getPlayerByUUID(tm);
			if (vpTeammate != null) {
				String tmHealth = (Bukkit.getPlayer(tm) == null) ? "LEBENDIG"
						: Math.max(0, Math.round(Bukkit.getPlayer(tm).getHealth() - tmDamage) / 2f) + " HP";
				varo.getScore(" §l§u§g" + Bukkit.getOfflinePlayer(tm).getName() + ": "
						+ ((vpTeammate.alive) ? "§a§l" + tmHealth : "§c§lTOT")).setScore(69);
				varo.getScore(" Kills: §2§l" + Math.round(vpTeammate.stats.get("kills")) + " ").setScore(68);
				varo.getScore(" Leben: §a§l" + 1 + " ").setScore(67);
				varo.getScore(" ").setScore(60);
			}
		}

		varo.getScore(" §7§oViel Glück!").setScore(1);

		p.setScoreboard(scoreboard);
	}

	public void updateScoreboard(VaroPlayer vp, double pDamage, double tmDamage) {
		Player p = Bukkit.getPlayer(vp.player);
		if (p != null)
			updateScoreboard(p, vp, pDamage, tmDamage);
	}
}
