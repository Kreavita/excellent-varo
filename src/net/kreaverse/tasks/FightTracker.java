package net.kreaverse.tasks;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroGame.GameState;

public class FightTracker extends BukkitRunnable {
	private VaroGame game;
	private VaroMessenger msg;

	public FightTracker(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

	@Override
	public void run() {
		if (game.state != GameState.ONGOING) {
			this.cancel();
			return;
		}

		game.players.stream().forEach((vp) -> {
			if (vp.attackedCooldown == 0)
				return;

			if (vp.attackedCooldown == 1) {
				vp.lastAttacker = null;
				msg.playerMessage(vp.player, "Du bist nicht mehr im Kampf und kannst dich jetzt ausloggen.",
						ChatColor.GREEN);
			}
			vp.attackedCooldown--;
		});
	}

}
