package net.kreaverse.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;

public class EntityDamageListener implements Listener {
	private VaroGame game;
	private VaroMessenger msg;

	public EntityDamageListener(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

	@EventHandler
	public void onEntityByEntityDamage(EntityDamageByEntityEvent e) {
		if (game.paused || game.getState() != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}

		if (e.getEntityType() != EntityType.PLAYER)
			return;

		Player victim = (Player) e.getEntity();
		VaroPlayer vpVictim = game.getPlayerByUUID(victim.getUniqueId());

		if (vpVictim == null || !vpVictim.alive) {
			e.setCancelled(true);
			return;
		}

		Entity damager = (e.getDamager() instanceof Projectile) ? (Entity) ((Projectile) e.getDamager()).getShooter()
				: e.getDamager();

		if (damager.getType() != EntityType.PLAYER) {
			game.playerHPChange(victim, e.getFinalDamage());
			return;
		}

		Player attacker = (Player) damager;
		VaroPlayer vpAttacker = game.getPlayerByUUID(attacker.getUniqueId());

		if (vpAttacker == null || !vpAttacker.alive) {
			e.setCancelled(true);
			return;
		}

		game.playerHPChange(victim, e.getFinalDamage());

		if (e.getFinalDamage() < victim.getHealth() && !vpVictim.equals(vpAttacker)) {
			if (vpVictim.lastAttacker == null) {
				msg.playerMessage(victim,
						"Du befindest dich jetzt im Kampf, in den nächsten 30 Sekunden darfst du dich nicht ausloggen!",
						ChatColor.YELLOW);
			}

			if (vpAttacker.lastAttacker == null) {
				msg.playerMessage(attacker,
						"Du befindest dich jetzt im Kampf, in den nächsten 30 Sekunden darfst du dich nicht ausloggen!",
						ChatColor.YELLOW);
			}
		}

		vpVictim.setAttacker(vpAttacker.player);
		vpAttacker.setAttacker(vpVictim.player);

		vpAttacker.incrementStat((e.getCause() == DamageCause.PROJECTILE) ? "shotsLanded" : "attacksLanded", 1);
		vpAttacker.incrementStat("shotsMissed", (e.getCause() == DamageCause.PROJECTILE) ? -1 : 0);

		vpAttacker.incrementStat("damageDealtToPlayers", e.getFinalDamage());
		vpVictim.incrementStat("damageTakenFromPlayers", e.getFinalDamage());
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e instanceof EntityDamageByEntityEvent)
			return;

		if (game.paused || game.getState() != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}

		if (e.getEntityType() != EntityType.PLAYER)
			return;

		Player victim = (Player) e.getEntity();
		VaroPlayer vpVictim = game.getPlayerByUUID(victim.getUniqueId());

		if (vpVictim == null || !vpVictim.alive) {
			e.setCancelled(true);
			return;
		}

		game.playerHPChange(victim, e.getFinalDamage());
	}

	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) {
			e.setCancelled(game.paused || game.getState() != GameState.ONGOING);
			return;
		}
		e.setCancelled(game.paused);
		game.playerHPChange((Player) e.getEntity(), e.getAmount());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (game.getState() != GameState.ONGOING) {
			return;
		}

		Player victim = e.getEntity();
		VaroPlayer vpVictim = game.getPlayerByUUID(victim.getUniqueId());

		if (vpVictim == null || !vpVictim.alive) {
			return;
		}

		VaroPlayer vpAttacker = game.getPlayerByUUID(vpVictim.lastAttacker);

		if (e.getPlayer().getKiller() != null && vpAttacker != null) {
			int killCount = (int) vpAttacker.incrementStat("kills", 1);
			msg.broadcastKillMessage(Bukkit.getOfflinePlayer(vpAttacker.player).getName(), e.deathMessage(), killCount,
					game.aliveCount);
		} else
			msg.broadcastDeathMessage(e.deathMessage(), game.aliveCount);

		game.playerKill(victim);

		e.getDrops().clear();
		e.setDroppedExp(0);
		e.setCancelled(
				victim.getLastDamageCause() == null || victim.getLastDamageCause().getCause() != DamageCause.VOID);
	}
}
