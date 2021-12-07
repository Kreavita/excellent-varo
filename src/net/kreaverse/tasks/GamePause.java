package net.kreaverse.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.kreaverse.ExcellentVARO;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;

public class GamePause extends BukkitRunnable {

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

	private VaroGame game;
	private VaroMessenger msg;
	private HashMap<Player, ArrayList<SavedPotion>> pausedEffects;
	private double pausedBorderSize;

	public GamePause(ExcellentVARO plugin, VaroMessenger msg, int minutes) {
		this.game = plugin.getGame();
		this.msg = msg;
		pausedEffects = new HashMap<Player, ArrayList<SavedPotion>>();

		pause(minutes);

		this.runTaskLater(plugin, 1200L * minutes);
	}

	@Override
	public void run() {
		game.paused = false;
		Bukkit.getServer().getWorlds().forEach(world -> {
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
			world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
			world.setGameRule(GameRule.DO_FIRE_TICK, true);
			if (pausedBorderSize < game.borderMaxSize) {
				world.getWorldBorder().setSize(game.borderMinSize,
						Math.round(60 * game.borderShrinkTime * (pausedBorderSize - game.borderMinSize)
								/ Math.max(1, (game.borderMaxSize - game.borderMinSize))));
			}
		});
		pausedEffects.forEach((player, effectList) -> {
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
			effectList.forEach(
					effect -> player.addPotionEffect(new PotionEffect(effect.type, effect.duration, effect.strength)));
		});

		Bukkit.getOnlinePlayers().forEach(player -> {
			if (player.getGameMode() != GameMode.CREATIVE || player.getGameMode() != GameMode.SPECTATOR) {
				player.setAllowFlight(false);
			}
			player.clearTitle();
		});

		try {
			this.cancel();
		} catch (IllegalStateException e) {
			Bukkit.getLogger().log(Level.INFO, "pauseTimer couldn't be cancelled");
		}
	}

	private void pause(int minutes) {
		game.paused = true;
		Bukkit.getServer().getWorlds().forEach(world -> {
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
			world.setGameRule(GameRule.DO_FIRE_TICK, false);

			pausedBorderSize = world.getWorldBorder().getSize();
			world.getWorldBorder().setSize(pausedBorderSize);
		});

		pausedEffects.clear();

		game.players.forEach(vp -> {
			if (!vp.alive || Bukkit.getPlayer(vp.player) == null)
				return;
			Player p = Bukkit.getPlayer(vp.player);

			ArrayList<SavedPotion> saved = new ArrayList<SavedPotion>();
			p.getActivePotionEffects().forEach(effect -> saved.add(new SavedPotion(effect)));
			pausedEffects.put(p, saved);

			if (p.getGameMode() != GameMode.CREATIVE || p.getGameMode() != GameMode.SPECTATOR) {
				p.setAllowFlight(false);
			}
			msg.pauseTitle(p, minutes);
		});
	}

}
