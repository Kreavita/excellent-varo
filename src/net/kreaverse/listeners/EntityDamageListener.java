package net.kreaverse.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;

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
		vpVictim.setAttacker(e.getEntity().getUniqueId());

		if (vpVictim == null || !vpVictim.alive) {
			e.setCancelled(true);
			return;
		}

		if (e.getDamager().getType() != EntityType.PLAYER) {
			if (e.getFinalDamage() < victim.getHealth()) {
				game.playerHPChange(victim, e.getFinalDamage());
				return;
			}
			msg.broadcastDeath(victim.getName(), game.aliveCount);
			game.playerKill(victim);
			e.setCancelled(true);
			return;
		}

		Player attacker = (Player) e.getDamager();
		VaroPlayer vpAttacker = game.getPlayerByUUID(attacker.getUniqueId());

		if (vpAttacker == null || !vpAttacker.alive) {
			e.setCancelled(true);
			return;
		}

		game.playerHPChange(victim, e.getFinalDamage());

		vpVictim.setAttacker(vpAttacker.player);
		vpAttacker.setAttacker(vpVictim.player);

		vpAttacker.incrementStat((e.getCause() == DamageCause.PROJECTILE) ? "shotsLanded" : "attacksLanded", 1);
		vpAttacker.incrementStat("shotsMissed", (e.getCause() == DamageCause.PROJECTILE) ? -1 : 0);

		vpAttacker.incrementStat("damageDealtToPlayers", e.getFinalDamage());
		vpVictim.incrementStat("damageTakenFromPlayers", e.getFinalDamage());

		if (e.getFinalDamage() < victim.getHealth()) {
			if (vpVictim.lastAttacker == null) {
				msg.playerMessage(vpVictim.player,
						"Du bist jetzt im Kampf, du darfst dich in den nächsten 30 Sekunden nicht ausloggen.",
						ChatColor.RED);
			}
			return;
		}

		int killCount = (int) vpAttacker.incrementStat("kills", 1);
		msg.broadcastKill(attacker.getName(), victim.getName(), killCount, game.aliveCount);

		game.playerKill(victim);
		e.setCancelled(true);
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

		if (e.getFinalDamage() < victim.getHealth())
			return;

		if (vpVictim.lastAttacker != null) {
			VaroPlayer vpAttacker = game.getPlayerByUUID(vpVictim.lastAttacker);
			int killCount = (int) vpAttacker.incrementStat("kills", 1);
			msg.broadcastKill(Bukkit.getOfflinePlayer(vpAttacker.player).getName(), victim.getName(), killCount,
					game.aliveCount);

		} else
			msg.broadcastDeath(victim.getName(), game.aliveCount);

		game.playerKill(victim);
		e.setCancelled(e.getCause() != DamageCause.VOID);
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
}
