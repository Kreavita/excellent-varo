package net.kreaverse.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.ExcellentVARO;
import net.kreaverse.tasks.BorderShrinker;
import net.kreaverse.tasks.FightTracker;
import net.kreaverse.tasks.StartCountdown;

public class VaroGame {

	public enum GameState {
		IDLE(0), ONGOING(1), FINISHED(2);

		public int toInt;

		GameState(int i) {
			this.toInt = i;
		}

		public static GameState fromInt(int x) {
			switch (x) {
			case 0:
				return IDLE;
			case 1:
				return ONGOING;
			case 2:
				return FINISHED;
			}
			return null;
		}
	}

	public GameState state;

	public ArrayList<VaroPlayer> players;
	public long aliveCount;

	public int borderSize;
	public int borderMinSize;
	public int borderShrinkTime; // 10 Minutes

	private int pauseCounter = 0;
	private int pauseDuration = 0;
	public boolean paused = false;

	private ExcellentVARO plugin;
	private VaroMessenger msg;

	private Timer timer = new Timer();
	private HashMap<String, BukkitRunnable> timerThreads;

	public VaroGame(ExcellentVARO plugin, @NotNull VaroMessenger msg) {
		this.plugin = plugin;
		this.msg = msg;

		borderSize = plugin.getConfig().getInt("game.borderSize");
		borderMinSize = plugin.getConfig().getInt("game.borderMinSize");
		borderShrinkTime = plugin.getConfig().getInt("game.borderShrinkTime");

		state = GameState.fromInt(plugin.getConfig().getInt("game.state"));

		pauseCounter = 0;
		pauseDuration = 0;
		paused = false;

		players = new ArrayList<VaroPlayer>();
		// TODO: restore Player stats from config

		timerThreads = new HashMap<String, BukkitRunnable>();

		if (state == GameState.ONGOING) {
			initIngameThreads();
		}

		Bukkit.getServer().getWorlds().forEach(world -> world.getWorldBorder().setSize(borderSize));
	}

	public void initIngameThreads() {
		if (timerThreads.get("borderShrinker") != null) {
			try {
				timerThreads.get("borderShrinker").cancel();
			} catch (IllegalStateException e) {
				plugin.getLogger().log(Level.INFO, "borderShrinker couldn't be cancelled");
			}
		}
		timerThreads.put("borderShrinker", new BorderShrinker(this, msg));
		timerThreads.get("borderShrinker").runTaskTimer(plugin, 20L * borderShrinkTime, 20L * borderShrinkTime);

		if (timerThreads.get("fightTracker") != null) {
			try {
				timerThreads.get("fightTracker").cancel();
			} catch (IllegalStateException e) {
				plugin.getLogger().log(Level.INFO, "fightTracker couldn't be cancelled");
			}
		}
		timerThreads.put("fightTracker", new FightTracker(this, msg));
		timerThreads.get("fightTracker").runTaskTimer(plugin, 1L, 20L);

		aliveCount = players.stream().filter(varoPlayer -> varoPlayer.alive).count();
		System.out.println(aliveCount);
	}

	public void start(int countdown) {
		if (state != GameState.IDLE) {
			return;
		}

		if (timerThreads.get("startCountdown") != null) {
			try {
				timerThreads.get("startCountdown").cancel();
			} catch (IllegalStateException e) {
				plugin.getLogger().log(Level.INFO, "startCountdown couldn't be cancelled");
			}
		}

		timerThreads.put("startCountdown", new StartCountdown(this, msg, countdown));
		timerThreads.get("startCountdown").runTaskTimer(plugin, 1L, 20L);
	}

	public long pause(int duration) {
		if (paused)
			return -1;

		if (pauseCounter == 0) {
			pauseDuration = duration;
		}
		pauseCounter++;

		long aliveOnlinePlayers = players.stream()
				.filter(varoPlayer -> varoPlayer.alive && Bukkit.getPlayer(varoPlayer.player) != null).count();

		if (pauseCounter < aliveOnlinePlayers / 2)
			return aliveOnlinePlayers / 2 - pauseCounter;

		pauseCounter = 0;
		paused = true;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				paused = false;
			}
		}, pauseDuration * 60000);
		return 0;
	}

	private void gameOver() {
		if (state != GameState.ONGOING)
			return;

		state = GameState.FINISHED;
		ArrayList<VaroPlayer> winners = getAlivePlayers();

		String broadcastMessage = "Varo ist vorbei.";
		if (winners.size() > 0) {
			broadcastMessage += " Gewinner:";
			for (VaroPlayer varoPlayer : winners) {
				broadcastMessage += " " + Bukkit.getOfflinePlayer(varoPlayer.player).getName() + ",";
				msg.playerMessage(varoPlayer.player,
						"Herzlichen Glückwunsch, {name}! Du hast das Exzellenz-VARO mit Note 1.0 bestanden! Lass dir den Sieg beim Prüfungsamt anrechnen!",
						ChatColor.GREEN);
				msg.playerTitle(varoPlayer.player, "GEWONNEN", ChatColor.GREEN, "Du hast gewonnen!",
						ChatColor.DARK_GREEN);
			}

			Bukkit.getOnlinePlayers()
					.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 1));

			broadcastMessage = broadcastMessage.substring(0, broadcastMessage.length() - 1) + ".";
		} else {
			broadcastMessage += "Es gab keine Gewinner.";
		}

		broadcastMessage += " Ein fettes Dankeschön an alle für die Teilnahme, war mega!";

		msg.broadcast(broadcastMessage, ChatColor.GREEN);
	}

	public void reset() {
		borderSize = plugin.getConfig().getInt("defaults.defaultBorder");
		state = GameState.IDLE;
		players.forEach(vp -> {
			vp.alive = true;
			if (Bukkit.getPlayer(vp.player) != null) {
				updatePlayer(Bukkit.getPlayer(vp.player));
			}
		});
		plugin.getServer().getWorlds().forEach(world -> {
			world.setTime(0);
		});
	}

	public VaroPlayer getPlayerByUUID(UUID uuid) {
		return players.stream().filter(varoPlayer -> varoPlayer.player.equals(uuid)).findFirst().orElse(null);
	}

	public ArrayList<VaroPlayer> getAlivePlayers() {
		return players.stream().filter(varoPlayer -> varoPlayer.alive).collect(Collectors.toCollection(ArrayList::new));
	}

	public void kill(@NotNull Player p) {
		if (state != GameState.ONGOING)
			return;

		p.getInventory().forEach(item -> {
			if (item != null)
				p.getWorld().dropItem(p.getLocation(), item);
		});

		p.getInventory().clear();

		aliveCount--;
		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());
		vp.alive = false;
		updatePlayer(p);

		msg.playerTitle(p, "TOT", ChatColor.DARK_RED, "Du bist gestorben - hast aber gut gekämpft!", ChatColor.RED);
		p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_DEATH, 1f, 0.5f);

		Bukkit.getOnlinePlayers().forEach(op -> {
			if (op == p)
				return;
			op.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
		});

		if (aliveCount == 2) {
			ArrayList<VaroPlayer> alivePlayers = getAlivePlayers();
			if (alivePlayers.get(0).player.equals(alivePlayers.get(1).teammate)
					|| alivePlayers.get(1).player.equals(alivePlayers.get(0).teammate)) {
				gameOver();
			}
		} else if (aliveCount <= 1)
			gameOver();
	}

	public void revive(@NotNull Player p) {
		if (state != GameState.ONGOING)
			return;

		aliveCount++;
		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());
		vp.alive = true;
		updatePlayer(p);
	}

	public void updatePlayer(@NotNull Player p) {
		switch (state) {
		case IDLE:
			p.setGameMode(GameMode.ADVENTURE);
			clearPlayer(p);
			break;
		case ONGOING:
			if (!getPlayerByUUID(p.identity().uuid()).alive) {
				p.setGameMode(GameMode.SPECTATOR);
			} else {
				p.setGameMode(GameMode.SURVIVAL);
			}
			break;
		case FINISHED:
			p.setGameMode(GameMode.SPECTATOR);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + state);
		}
	}

	private void clearPlayer(@NotNull Player p) {
		p.getInventory().clear();
		p.getActivePotionEffects().clear();
		p.setAbsorptionAmount(0);
		p.setTotalExperience(0);
		p.setArrowsInBody(0);
		p.setSaturation(20);
		p.setHealth(20);
		p.eject();
	}
}