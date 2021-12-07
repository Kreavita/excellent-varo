package net.kreaverse.listeners;

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

		VaroPlayer vpSpectator = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (vpSpectator == null || vpSpectator.alive || vpSpectator.getTeammate() == null
				|| Bukkit.getPlayer(vpSpectator.getTeammate()) == null)
			return;

		VaroPlayer vpTarget = game.getPlayerByUUID(vpSpectator.getTeammate());

		if (vpTarget == null || !vpTarget.alive)
			return;

		e.setCancelled(true);
		game.forceSpectate(e.getPlayer(), vpTarget);

	}

	@EventHandler
	public void onPlayerStopSpectating(PlayerStopSpectatingEntityEvent e) {
		if (e.getSpectatorTarget().getType() != EntityType.PLAYER || game.getState() != GameState.ONGOING)
			return;

		VaroPlayer vpSpectator = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (vpSpectator == null || vpSpectator.alive || vpSpectator.getTeammate() == null
				|| Bukkit.getPlayer(vpSpectator.getTeammate()) == null)
			return;

		VaroPlayer vpTarget = game.getPlayerByUUID(vpSpectator.getTeammate());

		if (vpTarget == null || !vpTarget.alive)
			return;

		e.setCancelled(true);

		if (vpTarget.player.equals(((Player) e.getSpectatorTarget()).getUniqueId())) {
			return;
		}
		
		game.forceSpectate(e.getPlayer(), vpTarget);
	}
}
