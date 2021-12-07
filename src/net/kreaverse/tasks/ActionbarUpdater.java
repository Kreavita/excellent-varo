package net.kreaverse.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroPlayer;
import net.kyori.adventure.text.Component;

public class ActionbarUpdater extends BukkitRunnable {

	private VaroGame game;
	private int threshold;

	public ActionbarUpdater(VaroGame game, int threshold) {
		this.game = game;
		this.threshold = threshold;
	}

	@Override
	public void run() {
		if (game.getState() != GameState.ONGOING) {
			return;
		}
		game.players.stream().filter(vp -> Bukkit.getPlayer(vp.player) != null)
				.forEach(vp -> updateActionBar(vp, Bukkit.getPlayer(vp.player)));
	}

	private void updateActionBar(VaroPlayer vp, Player p) {
		if (!vp.alive) {
			return;
		}
		double highestCoord = Math.max(Math.abs(p.getLocation().getX()), Math.abs(p.getLocation().getZ()));
		int distanceToBorder = (int) Math.max(0, (p.getWorld().getWorldBorder().getSize() / 2f) - highestCoord);

		if (distanceToBorder < threshold) {
			p.sendActionBar(Component.text(
					ChatColor.RED + "" + ChatColor.BOLD + "Distanz zur Border nur " + distanceToBorder + " Blöcke!"));
		}
	}

}
