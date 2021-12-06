package net.kreaverse.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

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
	public void onEntitySpawn(EntitySpawnEvent e) {
		e.setCancelled(game.paused || game.getState() != GameState.ONGOING);
	}

	@EventHandler
	public void onEntityMove(EntityMoveEvent e) {
		e.setCancelled(game.paused || game.getState() != GameState.ONGOING);
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		e.setCancelled(game.paused || game.getState() != GameState.ONGOING);
	}

	@EventHandler
	public void onCreeperIgnite(CreeperIgniteEvent e) {
		e.setCancelled(game.paused || game.getState() != GameState.ONGOING);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		e.setCancelled(game.paused || game.getState() != GameState.ONGOING);
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent e) {
		e.setCancelled(game.paused || game.getState() != GameState.ONGOING);
	}

	@EventHandler
	public void onEntityKnockbackByEntity(EntityKnockbackByEntityEvent e) {
		e.setCancelled(game.paused || game.getState() != GameState.ONGOING);
	}
}
