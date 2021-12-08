package net.kreaverse.tasks;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;

public class BorderShrinkDelay extends BukkitRunnable {
	private VaroGame game;
	private VaroMessenger msg;

	private int timer;
	private List<World> worlds;

	private double currentSize = 0;
	private long shrinkTime = 0;

	public BorderShrinkDelay(VaroGame game, VaroMessenger msg, List<World> worlds, int timer) {
		this.game = game;
		this.msg = msg;

		this.worlds = worlds;
		this.timer = timer;
	}

	@Override
	public void run() {
		if (game.paused)
			return;

		if (game.getState() != GameState.ONGOING) {
			this.cancel();
			return;
		}

		if (timer <= 0) {
			worlds.forEach(world -> {
				currentSize = world.getWorldBorder().getSize();
				if (currentSize > game.borderMinSize) {
					shrinkTime = Math.round(60 * game.borderShrinkTime * (currentSize - game.borderMinSize)
							/ Math.max(1, (game.borderMaxSize - game.borderMinSize)));
					world.getWorldBorder().setSize(game.borderMinSize, shrinkTime);
				}
			});

			if (currentSize > game.borderMinSize) {
				msg.broadcast(
						"Die Border beginnt jetzt von " + Math.round(currentSize) + " Blöcken auf "
								+ Math.round(game.borderMinSize) + " Blöcke zu schrumpfen in "
								+ ((shrinkTime / 60 == 0) ? shrinkTime + " Sekunden." : shrinkTime / 60 + " Minuten."),
						ChatColor.YELLOW);
			}

			this.cancel();
			return;
		}

		timer--;
	}
}
