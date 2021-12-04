package net.kreaverse.tasks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;

public class StartCountdown extends BukkitRunnable {
	private int counter;
	private VaroGame game;
	private VaroMessenger msg;

	public StartCountdown(VaroGame game, VaroMessenger msg, int countdown) {
		this.counter = countdown;
		this.game = game;
		this.msg = msg;
	}

	@Override
	public void run() {
		Bukkit.getLogger().log(Level.INFO, "Countdown Task Running, Counter: " + counter);
		if (counter == 0) {
			game.players.forEach(vp -> {
				msg.playerTitle(vp.player, "START", ChatColor.GREEN, "Exzellenz-Varo hat begonnen!",
						ChatColor.DARK_GREEN);
				Player p = Bukkit.getPlayer(vp.player);
				if (p == null)
					return;

				p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
				game.updatePlayer(p);
			});
			msg.broadcast("Mögen die Spiele beginnen!", ChatColor.GREEN);
			game.state = GameState.ONGOING;
			game.initIngameThreads();
			this.cancel();
			return;
		}

		if (counter == 60 || counter == 30 || counter == 15) {
			msg.broadcast("Noch " + counter + " Sekunden bis zum Start",
					(counter == 15) ? ChatColor.YELLOW : ChatColor.WHITE);

		} else if (counter <= 3) {
			msg.broadcast("Start in " + counter, ChatColor.RED);
			game.players.forEach(vp -> {
				msg.playerTitle(vp.player, "" + counter, ChatColor.YELLOW, "Exzellenz-Varo beginnt gleich!",
						ChatColor.GOLD);
			});
		}
		counter--;
	}
}
