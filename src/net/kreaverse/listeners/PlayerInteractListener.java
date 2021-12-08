package net.kreaverse.listeners;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroPlayer;

public class PlayerInteractListener implements Listener {
	private VaroGame game;

	public PlayerInteractListener(VaroGame game) {
		this.game = game;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (game.paused || game.getState() != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}

		VaroPlayer vp = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (vp == null) {
			e.setCancelled(true);
			return;
		}

		if (e.getAction().equals(Action.LEFT_CLICK_AIR) && vp.lastAttacker != null) {
			vp.incrementStat("attacksMissed", 1);
		}
	}

	@EventHandler
	public void onPlayerProjectileLaunch(PlayerLaunchProjectileEvent e) {
		if (game.paused || game.getState() != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}

		VaroPlayer vp = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (vp == null) {
			e.setCancelled(true);
			return;
		}
		vp.incrementStat("shotsMissed", 1); // This will be subtracted once the projectile hits its target.
	}

	@EventHandler
	public void onPlayerItemDrop(PlayerDropItemEvent e) {
		if (game.paused || game.getState() != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
		VaroPlayer vp = game.getPlayerByUUID(e.getPlayer().getUniqueId());
		if (vp != null && !vp.alive) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		e.setCancelled(game.paused);
	}

	@EventHandler
	public void onPlayerStartSpectating(PlayerStartSpectatingEntityEvent e) {
		if (game.getState() != GameState.ONGOING)
			return; // allowed, when game is not ongoing

		VaroPlayer vpSpectator = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (vpSpectator == null || vpSpectator.alive || vpSpectator.getTeammate() == null)
			return; // allowed, when event source player is not playing, alive or has no teammate

		VaroPlayer vpTeammate = game.getPlayerByUUID(vpSpectator.getTeammate());

		if (vpTeammate == null || !vpTeammate.alive)
			return; // allowed, when teammate does not exist or teammate dead too

		if (Bukkit.getPlayer(e.getCurrentSpectatorTarget().getUniqueId()) == null) {
			return; // this should never happen in the current implementation
		}

		if (e.getNewSpectatorTarget().getType() != EntityType.PLAYER) {
			e.setCancelled(true); // cancelled when spectating mobs
			return;
		}

		UUID targetUUID = ((Player) e.getNewSpectatorTarget()).getUniqueId();

		if (targetUUID != null && targetUUID.equals(vpTeammate.player))
			return; // allowed when target player is the teammate

		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerStopSpectating(PlayerStopSpectatingEntityEvent e) {
		if (e.getSpectatorTarget().getType() != EntityType.PLAYER || game.getState() != GameState.ONGOING)
			return; // allowed when current target is not a player or game is not running

		VaroPlayer vpSpectator = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (vpSpectator == null || vpSpectator.alive || vpSpectator.getTeammate() == null)
			return; // allowed, when event source player is not playing, alive or has no teammate

		VaroPlayer vpTeammate = game.getPlayerByUUID(vpSpectator.getTeammate());

		if (vpTeammate == null || !vpTeammate.alive)
			return; // allowed, when teammate does not exist or teammate dead too

		UUID targetUUID = ((Player) e.getSpectatorTarget()).getUniqueId();

		if (Bukkit.getPlayer(targetUUID) == null) {
			return; // this should never happen in the current implementation
		}

		if (targetUUID == null || !targetUUID.equals(vpTeammate.player)) {
			Bukkit.getLogger().log(Level.INFO,
					"StopSpectating: Player currently not spectating the target, resetting ...");
			game.forceSpectate(e.getPlayer(), vpTeammate);
			return;
		}

		e.setCancelled(true);
	}
}
