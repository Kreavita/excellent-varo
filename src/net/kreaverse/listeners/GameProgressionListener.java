package net.kreaverse.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.world.TimeSkipEvent;

import com.destroystokyo.paper.event.entity.CreeperIgniteEvent;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;

import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;

public class GameProgressionListener implements Listener {
	private VaroGame game;

	public GameProgressionListener(VaroGame game) {
		this.game = game;
	}

	@EventHandler
	public void onTimeSkip(TimeSkipEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onEntityMove(EntityMoveEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}
	@EventHandler
	public void onCreeperIgnite(CreeperIgniteEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}
	@EventHandler
	public void onEntityKnockbackByEntity(EntityKnockbackByEntityEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}
}
