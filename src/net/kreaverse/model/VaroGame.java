package net.kreaverse.model;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VaroGame {

	public enum GameState {
		IDLE, ONGOING, FINISHED
	}

	private static @NotNull VaroGame game = new VaroGame();

	private VaroGame() {
	}

	public static VaroGame getInstance() {
		return game;
	}

	FileConfiguration config;
	public GameState state = GameState.IDLE;
	public ArrayList<VaroPlayer> players = new ArrayList<VaroPlayer>();
	public int borderSize = 200;
	public long aliveCount = 0;

	public void restore() {
		config = VaroConfig.getInstance().configFile;
		// TODO: restore Game state from config
	}

	public void start() {
		aliveCount = players.stream().filter(varoPlayer -> varoPlayer.alive).count();
		// TODO: start the game, countdown and stuff
		state = GameState.ONGOING;
	}

	public void reset() {
		borderSize = config.getInt("defaultBorder");
		// TODO: reset the whole game, every inventory, the map maybe?
		state = GameState.IDLE;
	}

	public VaroPlayer getPlayerByUUID(UUID uuid) {
		return players.stream().filter(varoPlayer -> varoPlayer.player == uuid).findFirst().orElse(null);
	}

	public void kill(Player p) {

		p.getInventory().forEach(item -> p.getWorld().dropItem(p.getLocation(), item));
		p.getInventory().clear();

		aliveCount--;
		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());
		vp.alive = false;
		updatePlayer(p);
	}

	public void revive(Player p) {
		aliveCount++;
		VaroPlayer vp = getPlayerByUUID(p.getUniqueId());
		vp.alive = true;
		updatePlayer(p);
	}

	public void updatePlayer(Player p) {
		switch (state) {
		case IDLE:
			p.setGameMode(GameMode.ADVENTURE);
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
}
