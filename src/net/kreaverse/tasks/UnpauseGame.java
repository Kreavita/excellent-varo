package net.kreaverse.tasks;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.kreaverse.model.VaroGame;

public class UnpauseGame extends BukkitRunnable {
	private VaroGame game;

	private class SavedPotion {
		private PotionEffectType type;
		private int duration;
		private int strength;

		SavedPotion(PotionEffectType type, int strength, int duration) {
			this.type = type;
			this.duration = duration;
			this.strength = strength;
		}

		SavedPotion(PotionEffect effect) {
			this(effect.getType(), effect.getAmplifier(), effect.getDuration());
		}
	}

	private HashMap<Player, ArrayList<SavedPotion>> pausedEffects;

	public UnpauseGame(VaroGame game) {
		this.game = game;
		pausedEffects = new HashMap<Player, ArrayList<SavedPotion>>();
		pause();
	}

	@Override
	public void run() {
		game.paused = false;
		pausedEffects.forEach((player, effectList) -> {
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
			effectList.forEach(
					effect -> player.addPotionEffect(new PotionEffect(effect.type, effect.duration, effect.strength)));
			player.setAllowFlight(false);
		});
	}

	private void pause() {
		game.paused = true;
		Bukkit.getServer().getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false));
		game.players.forEach(vp -> {
			if (!vp.alive || Bukkit.getPlayer(vp.player) == null)
				return;
			Player p = Bukkit.getPlayer(vp.player);
			
			ArrayList<SavedPotion> saved = new ArrayList<SavedPotion>();
			p.getActivePotionEffects().forEach(effect -> saved.add(new SavedPotion(effect)));
			pausedEffects.put(p, saved);
			
			p.setAllowFlight(true);
		});
	}

}
