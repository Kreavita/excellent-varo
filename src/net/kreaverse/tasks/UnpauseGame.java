package net.kreaverse.tasks;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import net.kreaverse.model.VaroGame;

public class UnpauseGame extends BukkitRunnable {
	private VaroGame game;

	private HashMap<Player, Collection<PotionEffect>> pausedEffects;

	public UnpauseGame(VaroGame game) {
		this.game = game;
		pausedEffects = new HashMap<Player, Collection<PotionEffect>>();
		pause();
	}

	@Override
	public void run() {
		game.paused = false;
		Bukkit.getServer().getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true));
		pausedEffects.forEach((player, effectList) -> {
			player.getActivePotionEffects().clear();
			effectList.forEach(effect -> player.addPotionEffect(effect));
		});
	}

	private void pause() {
		game.paused = true;
		Bukkit.getServer().getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false));
		game.players.forEach(vp -> {
			if (!vp.alive || Bukkit.getPlayer(vp.player) == null)
				return;
			Player p = Bukkit.getPlayer(vp.player);
			pausedEffects.put(p, p.getActivePotionEffects());
		});
	}

}
