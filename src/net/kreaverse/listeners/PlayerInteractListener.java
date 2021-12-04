package net.kreaverse.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;

public class PlayerInteractListener implements Listener {
	private VaroGame game;
	private VaroMessenger msg;

	public PlayerInteractListener(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

	public void onPlayerInteract(PlayerInteractEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}

		VaroPlayer vp = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (vp == null)
			return;

		if (e.getAction().equals(Action.LEFT_CLICK_AIR)) {
			vp.incrementStat("AttacksMissed", 1);
		}
	}

	public void onPlayerProjectileLaunch(PlayerLaunchProjectileEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}

		VaroPlayer vp = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (vp == null)
			return;

		vp.incrementStat("ShotsMissed", 1); // This will be subtracted once the projectile hits its target.
	}
}
