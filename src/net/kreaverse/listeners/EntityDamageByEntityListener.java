package net.kreaverse.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroPlayer;

public class EntityDamageByEntityListener implements Listener {

	VaroGame game = VaroGame.getInstance();
	VaroMessenger msg = VaroMessenger.getInstance();
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
//		System.out.println("EntityDamageByEntityListener.onEntityDamage()");
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

		if (e.getDamager().getType() == EntityType.PLAYER) {
			Player attacker = (Player) e.getDamager();
			VaroPlayer vpAttacker = game.getPlayerByUUID(attacker.getUniqueId());

			if (vpAttacker == null || !vpAttacker.alive) {
				e.setCancelled(true);
				return;
			}

			vpAttacker.incrementStat((e.getCause() == DamageCause.PROJECTILE) ? "shotsLanded" : "attacksLanded", 1);
			vpAttacker.incrementStat("damageDealt", e.getFinalDamage());

			if (e.isCritical()) {
				int killCount = (int) vpAttacker.incrementStat("kills", 1);
				game.kill(victim);
				msg.killMsg(attacker, victim, killCount, game.aliveCount);
			}

		} else {
			if (e.isCritical()) {
				game.kill(victim);
				msg.deathMsg(victim, game.aliveCount);
			}
		}
	}
}
