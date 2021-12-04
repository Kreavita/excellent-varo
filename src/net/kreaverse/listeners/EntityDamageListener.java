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
		if (game.state != GameState.ONGOING) {
			e.setCancelled(true);
			return;
		}

		if (e.getEntityType() != EntityType.PLAYER)
			return;

		Player victim = (Player) e.getEntity();
		VaroPlayer vpVictim = game.getPlayerByUUID(victim.getUniqueId());
		vpVictim.attacked(e.getEntity().getUniqueId());
		if (vpVictim == null || !vpVictim.alive) {
			e.setCancelled(true);
			return;
		}

		if (e.getDamager().getType() != EntityType.PLAYER) {
			if (e.getFinalDamage() < victim.getHealth())
				return;

			msg.broadcastDeath(victim.getName(), game.aliveCount - 1);
			game.kill(victim);
			e.setCancelled(true);
			return;
		}

		Player attacker = (Player) e.getDamager();
		VaroPlayer vpAttacker = game.getPlayerByUUID(attacker.getUniqueId());

		if (vpAttacker == null || !vpAttacker.alive) {
			e.setCancelled(true);
			return;
		}

		vpVictim.attacked(vpAttacker.player);
		vpAttacker.attacked(vpVictim.player);
		vpAttacker.incrementStat((e.getCause() == DamageCause.PROJECTILE) ? "shotsLanded" : "attacksLanded", 1);
		vpAttacker.incrementStat("damageDealt", e.getFinalDamage());
		vpVictim.incrementStat("damageTaken", e.getFinalDamage());

		if (e.getFinalDamage() >= victim.getHealth()) {
			int killCount = (int) vpAttacker.incrementStat("kills", 1);
			msg.broadcastKill(attacker.getName(), victim.getName(), killCount, game.aliveCount - 1);
			game.kill(victim);
			return;
		} else if (vpVictim.lastAttacker == null) {
			msg.playerMessage(vpVictim.player,
					"Du bist jetzt im Kampf, du darfst dich in den nächsten 30 Sekunden nicht ausloggen.",
					ChatColor.RED);
		}

		vpVictim.attacked(vpAttacker.player);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e instanceof EntityDamageByEntityEvent)
			return;

		if (e.getCause() == DamageCause.VOID)
			return;

//		Bukkit.getLogger().log(Level.INFO, "Damage Not caused by Entity");

		if (game.state != GameState.ONGOING) {
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

		if (e.getFinalDamage() < victim.getHealth())
			return;

		if (vpVictim.lastAttacker != null) {
			VaroPlayer vpAttacker = game.getPlayerByUUID(vpVictim.lastAttacker);
			int killCount = (int) vpAttacker.incrementStat("kills", 1);
			msg.broadcastKill(Bukkit.getOfflinePlayer(vpAttacker.player).getName(), victim.getName(), killCount,
					game.aliveCount - 1);

		} else
			msg.broadcastDeath(victim.getName(), game.aliveCount);

		game.kill(victim);
	}
}
