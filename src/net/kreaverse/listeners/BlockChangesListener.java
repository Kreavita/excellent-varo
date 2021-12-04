package net.kreaverse.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;

public class BlockChangesListener implements Listener {
	private VaroGame game;

	public BlockChangesListener(VaroGame game) {
		this.game = game;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBlockBurn(BlockBurnEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBlockDrop(BlockDropItemEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBlockGrow(BlockGrowEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onTNTPrime(TNTPrimeEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onLeavesDecayEvent(LeavesDecayEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onFluidLevelChange(FluidLevelChangeEvent e) {
		if (game.paused || game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}
	}
}
