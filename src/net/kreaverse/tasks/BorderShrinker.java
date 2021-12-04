package net.kreaverse.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;

public class BorderShrinker extends BukkitRunnable {
	private VaroGame game;
	private VaroMessenger msg;

	public BorderShrinker(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

	@Override
	public void run() {
		if (game.state != GameState.ONGOING || game.borderSize == game.borderMinSize) {
			this.cancel();
			return;
		}

		game.borderSize = Math.max(game.borderMinSize, game.borderSize - 100);
		Bukkit.getServer().getWorlds().forEach(world -> world.getWorldBorder().setSize(game.borderSize));
		msg.broadcast("Die Border ist geschrumpft und beträgt jetzt " + game.borderSize + " Blöcke.", ChatColor.YELLOW);
	}
}
