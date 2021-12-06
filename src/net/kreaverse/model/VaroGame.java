package net.kreaverse.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.ExcellentVARO;
import net.kreaverse.listeners.GlowHandler;
import net.kreaverse.tasks.FightTracker;
import net.kreaverse.tasks.ScoreboardUpdater;
import net.kreaverse.tasks.StartCountdown;
import net.kreaverse.tasks.UnpauseGame;

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

	public ArrayList<VaroPlayer> players;
	public long aliveCount;

	public int borderSize;
	public int defaultBorderSize;
	public int borderMinSize;
	public int borderShrinkTime;

	public boolean paused = false;

	private GameState state;

	private ExcellentVARO plugin;
	private VaroMessenger msg;
	public GlowHandler pl;

	private HashMap<String, BukkitRunnable> timerThreads;
	private ScoreboardUpdater scoreboardUpdater;

	public VaroGame(ExcellentVARO plugin, @NotNull VaroMessenger msg, @NotNull VaroConfig cfg) {
		this.plugin = plugin;
		this.msg = msg;
		this.scoreboardUpdater = new ScoreboardUpdater(this);
		scoreboardUpdater.runTaskTimer(plugin, 1L, 100L);

		paused = false;
		borderSize = plugin.getConfig().getInt("border.borderSize");

		borderMinSize = plugin.getConfig().getInt("defaults.borderMinSize");
		defaultBorderSize = plugin.getConfig().getInt("defaults.defaultBorder");
		borderShrinkTime = plugin.getConfig().getInt("defaults.borderShrinkTime");

		players = cfg.loadPlayers();
		timerThreads = new HashMap<String, BukkitRunnable>();

		pl = new GlowHandler(this, plugin);

		updateState(GameState.fromInt(plugin.getConfig().getInt("game.state")));
	}

	public void updateState(GameState newState) {
		if (state == newState)
			return;

		state = newState;

		switch (newState) {
		case IDLE:
			Bukkit.getServer().getWorlds().forEach(world -> {
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
				world.setTime(0);
				world.getWorldBorder().setSize(defaultBorderSize);
			});

			players.forEach(vp -> {
				vp.alive = true;
				if (Bukkit.getPlayer(vp.player) != null) {
					Player p = Bukkit.getPlayer(vp.player);
					updatePlayer(p);
					p.teleport(p.getWorld().getSpawnLocation());
				}
			});
			break;

		case FINISHED:
			Bukkit.getServer().getWorlds().forEach(world -> {
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
				world.getWorldBorder().setSize(world.getWorldBorder().getSize());
			});
			break;

		case ONGOING:
			Bukkit.getServer().getWorlds().forEach(world -> {
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
				world.getWorldBorder().setSize(borderMinSize, Math.round(
						((float) ((defaultBorderSize - borderSize) * borderShrinkTime) / (float) defaultBorderSize)));
			});
			initIngameThreads();
			break;

		default:
			break;
		}
		scoreboardUpdater.run();
	}

	public void updatePlayer(@NotNull Player p) {
		switch (state) {
		case IDLE:
			playerClear(p);
			p.setGameMode(GameMode.ADVENTURE);
			break;
		case ONGOING:
			playerClear(p);
			VaroPlayer vp = getPlayerByUUID(p.identity().uuid());
			if (!vp.alive) {
				p.setGameMode(GameMode.SPECTATOR);
				msg.playerMessage(p, "Falls du ein Teammate hast, wirst du in 10 Sekunden zu ihm teleportiert...",
						ChatColor.GRAY);

				if (timerThreads.get("forceSpectateTimer") != null
						&& !timerThreads.get("forceSpectateTimer").isCancelled()) {
					timerThreads.get("forceSpectateTimer").cancel();
				}

				timerThreads.put("forceSpectateTimer", new BukkitRunnable() {
					@Override
					public void run() {
						forceSpectate(p, getPlayerByUUID(vp.getTeammate()));
					}
				});
				timerThreads.get("forceSpectateTimer").runTaskLater(plugin, 20L * 10);
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

	public GameState getState() {
		return state;
	}

	public VaroPlayer getPlayerByUUID(UUID uuid) {
		return players.stream().filter(varoPlayer -> varoPlayer.player.equals(uuid)).findFirst().orElse(null);
	}

	private ArrayList<VaroPlayer> getAlivePlayers() {
		return players.stream().filter(varoPlayer -> varoPlayer.alive).collect(Collectors.toCollection(ArrayList::new));
	}

	private void initIngameThreads() {
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

	public void pause(int duration, String name) {
		if (paused)
			return;

		msg.broadcast(name + " hat das Spiel wird für " + duration + " Minuten pausiert!", ChatColor.YELLOW);

		if (timerThreads.get("pauseTimer") != null) {
			try {
				timerThreads.get("pauseTimer").cancel();
			} catch (IllegalStateException e) {
				plugin.getLogger().log(Level.INFO, "pauseTimer couldn't be cancelled");
			}
		}

		timerThreads.put("pauseTimer", new UnpauseGame(this));
		timerThreads.get("pauseTimer").runTaskLater(plugin, 1200L * duration);
	}

	public void resume(String name) {
		if (!paused)
			return;

		if (timerThreads.get("pauseTimer") == null
				|| (timerThreads.get("resumeTimer") != null && !timerThreads.get("resumeTimer").isCancelled()))
			return;

		msg.broadcast(name + " hebt die Pause auf. Das Spiel wird in 5 Sekunden fortgesetzt...", ChatColor.YELLOW);
		Bukkit.getServer().getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true));

		timerThreads.put("resumeTimer", new BukkitRunnable() {
			@Override
			public void run() {
				timerThreads.get("pauseTimer").run();
				try {
					timerThreads.get("pauseTimer").cancel();
				} catch (IllegalStateException e) {
					plugin.getLogger().log(Level.INFO, "pauseTimer couldn't be cancelled");
				}
				timerThreads.replace("pauseTimer", null);
			}
		});
		timerThreads.get("resumeTimer").runTaskLater(plugin, 20L * 5);
	}

	private void gameOver() {
		if (state != GameState.ONGOING)
			return;

		updateState(GameState.FINISHED);
		ArrayList<VaroPlayer> winners = getAlivePlayers();

		String broadcastMessage = "Varo ist vorbei. ";
		if (winners.size() > 0) {
			broadcastMessage += "Gewinner:";
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

	public VaroPlayer playerJoin(Player p) {
		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());

		if (vp == null) {
			vp = new VaroPlayer(p.getUniqueId());
			vp.alive = state == GameState.IDLE;
			players.add(vp);
		}

		if (!vp.alive) {
			msg.playerTitle(p, "Zuschauer", ChatColor.GRAY, "Das Spiel hat bereits begonnen oder du bist bereits tot",
					ChatColor.LIGHT_PURPLE);
		}
		updatePlayer(p);
		scoreboardUpdater.run();
		return vp;
	}

	public void updateTeamGlow(Player p1, Player p2) {
		if (p1 != null) {
			pl.sendUpdatePackets(p1);
		}
		if (p2 != null) {
			pl.sendUpdatePackets(p2);
		}
	}

	public void playerHPChange(Player p, double damage) {
		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());

		if (vp == null)
			return;

		scoreboardUpdater.updateScoreboard(vp, damage, 0);

		VaroPlayer teammate = getPlayerByUUID(vp.getTeammate());

		if (teammate == null)
			return;

		scoreboardUpdater.updateScoreboard(teammate, 0, damage);
	}

	public void playerKill(@NotNull VaroPlayer vp) {
		aliveCount--;
		vp.incrementStat("deaths", 1);
		vp.alive = false;

		if (aliveCount == 2) {
			ArrayList<VaroPlayer> alivePlayers = getAlivePlayers();
			if (alivePlayers.get(0).player.equals(alivePlayers.get(1).getTeammate())
					|| alivePlayers.get(1).player.equals(alivePlayers.get(0).getTeammate())) {
				gameOver();
			}
		} else if (aliveCount <= 1)
			gameOver();

		scoreboardUpdater.run();
	}

	public void playerKill(@NotNull Player p) {
		if (state != GameState.ONGOING)
			return;

		p.getInventory().forEach(item -> {
			if (item != null)
				p.getWorld().dropItem(p.getLocation(), item);
		});

		p.getInventory().clear();

		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());

		msg.playerTitle(p, "TOT", ChatColor.DARK_RED, "Du bist gestorben - hast aber gut gekämpft!", ChatColor.RED);
		p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_DEATH, 1f, 0.5f);

		Bukkit.getOnlinePlayers().forEach(op -> {
			if (op == p)
				return;
			op.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
		});

		playerKill(vp);
		updatePlayer(p);

		scoreboardUpdater.updateScoreboard(vp);
	}

	public void playerRevive(@NotNull Player p) {
		if (state != GameState.ONGOING)
			return;

		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());
		playerRevive(vp);

		updatePlayer(p);
		scoreboardUpdater.updateScoreboard(vp);
	}

	public void playerRevive(@NotNull VaroPlayer vp) {
		if (state != GameState.ONGOING)
			return;
		aliveCount++;
		vp.alive = true;
		scoreboardUpdater.run();
	}

	public void forceSpectate(@NotNull Player spectator, VaroPlayer vpTeammate) {
		if (spectator.getGameMode() != GameMode.SPECTATOR)
			return; // Player is not a spectator

		if (vpTeammate == null || !vpTeammate.alive)
			return; // has no Teammate or both are dead

		Player teammate = Bukkit.getPlayer(vpTeammate.player);

		if (teammate == null)
			return; // Teammate is offline

		spectator.setSpectatorTarget(teammate);
		return;
	}

	private void playerClear(@NotNull Player p) {
		p.getInventory().clear();
		p.setStatistic(Statistic.TIME_SINCE_REST, 0);
		p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
		p.setAbsorptionAmount(0);
		p.setTotalExperience(0);
		p.setArrowsInBody(0);
		p.setFoodLevel(20);
		p.setHealth(20);
		p.eject();
	}
}