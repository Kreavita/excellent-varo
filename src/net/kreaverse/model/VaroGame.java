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
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.ExcellentVARO;
import net.kreaverse.listeners.GlowHandler;
import net.kreaverse.tasks.ActionbarUpdater;
import net.kreaverse.tasks.BorderShrinkDelay;
import net.kreaverse.tasks.FightTracker;
import net.kreaverse.tasks.GamePause;
import net.kreaverse.tasks.ScoreboardUpdater;
import net.kreaverse.tasks.StartCountdown;
import net.kyori.adventure.text.Component;

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

	public double borderMinSize;
	public double borderMaxSize;
	public double borderShrinkTime; // Minutes
	public int borderShrinkDelay; // Minutes

	public boolean paused = false;
	public boolean shrinkDelayed = false;

	private GameState state;

	private ExcellentVARO plugin;
	private VaroMessenger msg;
	public GlowHandler gh;

	private HashMap<String, BukkitRunnable> timerThreads;
	private ScoreboardUpdater scoreboardUpdater;

	public VaroGame(ExcellentVARO plugin, @NotNull VaroMessenger msg, @NotNull VaroConfig cfg) {
		this.plugin = plugin;
		this.msg = msg;
		this.scoreboardUpdater = new ScoreboardUpdater(this);
		scoreboardUpdater.runTaskTimer(plugin, 1L, 100L);

		paused = false;

		borderMaxSize = plugin.getConfig().getInt("defaults.borderMaxSize");
		borderMinSize = plugin.getConfig().getInt("defaults.borderMinSize");
		borderShrinkTime = plugin.getConfig().getInt("defaults.borderShrinkTime");
		borderShrinkDelay = plugin.getConfig().getInt("defaults.borderShrinkDelay");

		players = cfg.loadPlayers();
		timerThreads = new HashMap<String, BukkitRunnable>();

		gh = new GlowHandler(this, plugin);

		int savedBorderSize = plugin.getConfig().getInt("game.borderSize");
		Bukkit.getServer().getWorlds().forEach(world -> {
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
			world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 50);
			world.setGameRule(GameRule.UNIVERSAL_ANGER, false);
//			world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
			world.getWorldBorder().setSize(savedBorderSize);
		});

		int borderWarningBlocks = plugin.getConfig().getInt("defaults.borderWarningDistance");
		(new ActionbarUpdater(this, borderWarningBlocks)).runTaskTimer(plugin, 1L, 1L);

		updateState(GameState.fromInt(plugin.getConfig().getInt("game.state")));

//		(new BukkitRunnable() {
//
//			@Override
//			public void run() {
//				Bukkit.getOnlinePlayers().forEach(spectator -> {
//					VaroPlayer vpSpectator = getPlayerByUUID(spectator.getUniqueId());
//
//					if (vpSpectator == null || vpSpectator.alive || vpSpectator.getTeammate() == null
//							|| Bukkit.getPlayer(vpSpectator.getTeammate()) == null)
//						return;
//
//  					forceSpectate(spectator, getPlayerByUUID(vpSpectator.getTeammate()));
//				});
//			}
//		}).runTaskTimer(plugin, 20L, 100L);
	}

	public void updateState(GameState newState) {
		if (state == newState)
			return;

		state = newState;

		switch (newState) {
		case IDLE:
			Bukkit.getServer().getWorlds().forEach(world -> {
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
				world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
				world.setGameRule(GameRule.DO_FIRE_TICK, false);
				world.setTime(0);
				world.getWorldBorder().setSize(borderMaxSize);
			});

			players.forEach(vp -> {
				vp.alive = true;
				vp.revivesLeft = 1;
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
				world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
				world.setGameRule(GameRule.DO_FIRE_TICK, false);
				world.getWorldBorder().setSize(world.getWorldBorder().getSize());
			});
			break;

		case ONGOING:
			Bukkit.getServer().getWorlds().forEach(world -> {
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
				world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
				world.setGameRule(GameRule.DO_FIRE_TICK, true);
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
			VaroPlayer vp = getPlayerByUUID(p.identity().uuid());
			if (!vp.alive) {
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
		(new BorderShrinkDelay(this, msg, Bukkit.getServer().getWorlds(), shrinkDelayed ? borderShrinkDelay : 0))
				.runTaskTimer(plugin, 20, 1200);
		;

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
		timerThreads.put("startCountdown", new StartCountdown(plugin, msg, countdown));
	}

	public void pause(int duration, CommandSender p) {
		if (paused)
			return;

		msg.broadcast(p.getName() + " hat das Spiel wird für " + duration
				+ " Minuten pausiert! Benutze '/unpause' um die Pause abzubrechen", ChatColor.YELLOW);

		if (timerThreads.get("pauseTimer") != null) {
			try {
				timerThreads.get("pauseTimer").cancel();
			} catch (IllegalStateException e) {
				plugin.getLogger().log(Level.INFO, "pauseTimer couldn't be cancelled");
			}
		}

		timerThreads.put("pauseTimer", new GamePause(plugin, msg, duration));
	}

	public void resume(CommandSender p) {
		if (!paused)
			return;

		if (timerThreads.get("pauseTimer") == null || timerThreads.get("resumeTimer") != null) {
			msg.errorMessage(p, "Das Spiel wird bereits fortgesetzt.");
			return;
		}

		msg.broadcast(p.getName() + " hebt die Pause auf. Das Spiel wird in 5 Sekunden fortgesetzt...",
				ChatColor.YELLOW);

		timerThreads.put("resumeTimer", new BukkitRunnable() {
			@Override
			public void run() {
				timerThreads.get("pauseTimer").run();
				this.cancel();
				timerThreads.put("resumeTimer", null);
				msg.broadcast("Das Spiel geht weiter!", ChatColor.GREEN);
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
					.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1));

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
			vp = new VaroPlayer(p.getUniqueId(), plugin.getConfig().getInt("defaults.playerRevives"));
			vp.alive = state == GameState.IDLE;
			players.add(vp);
		}

		updatePlayer(p);

		if (!vp.alive) {
			if (vp.getTeammate() != null && getPlayerByUUID(vp.getTeammate()).alive
					&& Bukkit.getPlayer(vp.getTeammate()) == null) {
				p.kick(Component.text(ChatColor.RED
						+ "Du bist tot und kannst gerade nicht zuschauen, da dein Teammate noch lebt und nicht online ist."));
			}

			msg.playerTitle(p, "Zuschauer", ChatColor.GRAY, "Das Spiel hat bereits begonnen oder du bist tot",
					ChatColor.LIGHT_PURPLE);

			(new BukkitRunnable() {

				@Override
				public void run() {
					forceSpectate(p, getPlayerByUUID(getPlayerByUUID(p.getUniqueId()).getTeammate()));
				}
			}).runTaskLater(plugin, 20L);

		} else {
			p.clearTitle();
		}

		scoreboardUpdater.run();
		return vp;
	}

	public void updateTeamGlow(Player p1, Player p2) {
		if (p1 != null && p2 != null) {
			gh.initiateTeamGlow(p1, p2);
			gh.initiateTeamGlow(p2, p1);
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

		int level = Math.min(p.getLevel(), 15);
		float progress = p.getExp();
		while (level > 0) {
			ExperienceOrb e = (ExperienceOrb) p.getWorld().spawnEntity(new Location(p.getWorld(), 0, 0, 0),
					EntityType.EXPERIENCE_ORB);
			e.teleport(p.getLocation());
			e.setExperience(Math.round((experienceToDrop(level, progress) - experienceToDrop(level - 1, 0)) / 2f));
			level--;
			progress = 0;
		}
		playerClear(p);

		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());

		msg.playerTitle(p, "TOT", ChatColor.RED, "Du bist gestorben - hast aber gut gekämpft!", ChatColor.LIGHT_PURPLE);
		p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_DEATH, 1f, 0.5f);

		Bukkit.getOnlinePlayers().forEach(op -> {
			if (op == p)
				return;
			op.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
		});

		playerKill(vp);
		updatePlayer(p);

		msg.playerMessage(p, "Falls du ein Teammate hast, wirst du in 10 Sekunden zu ihm teleportiert...",
				ChatColor.GRAY);
		(new BukkitRunnable() {
			@Override
			public void run() {
				forceSpectate(p, getPlayerByUUID(vp.getTeammate()));
			}
		}).runTaskLater(plugin, 20L * 10);

		scoreboardUpdater.updateScoreboard(vp);
	}

	/*
	 * See: https://minecraft.fandom.com/wiki/Experience#Leveling_up
	 */
	private float experienceToDrop(int level, float progress) {
		if (level < 16) {
			return level * level + 6 * level + progress * (2 * level + 7);
		} else if (level < 32) {
			return 2.5f * level * level - 40.5f * level + 360 + progress * (5 * level - 38);
		} else {
			return 4.5f * level * level - 162.5f * level + 2200 + progress * (9 * level - 158);
		}
	}

	public void playerRevive(@NotNull Player p) {
		if (state != GameState.ONGOING)
			return;

		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());
		playerRevive(vp);

		msg.playerTitle(p, "WIEDERBELEBT", ChatColor.GREEN, "Du lebst jetzt wieder - viel Glück!", ChatColor.AQUA);
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
			return; // has no Teammate or Teammate is dead too

		Player teammate = Bukkit.getPlayer(vpTeammate.player);

		if (teammate == null) // Teammate is offline
			spectator.kick(Component.text(ChatColor.RED
					+ "Du bist tot und kannst gerade nicht zuschauen, da dein Teammate noch lebt und nicht online ist."));

		if (spectator.getSpectatorTarget() != null && spectator.getSpectatorTarget().equals(teammate)) {
			Bukkit.getLogger().log(Level.INFO, "ForceSpectate: Player already spectating the target, returning...");
			return; // Player already Spectating the target
		}

		msg.playerMessage(spectator, "Da dein Teammate noch lebt, kannst du nicht frei zuschauen!",
				ChatColor.LIGHT_PURPLE);
		spectator.setSpectatorTarget(teammate);
		return;
	}

	public void playerClear(@NotNull Player p) {
		p.getInventory().clear();
		p.setStatistic(Statistic.TIME_SINCE_REST, 0);
		p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
		p.setAbsorptionAmount(0);
		p.setLevel(0);
		p.setExp(0);
		p.setArrowsInBody(0);
		p.setFoodLevel(20);
		p.setHealth(20);
		p.setFireTicks(0);
		p.eject();
	}
}