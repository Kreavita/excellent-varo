package net.kreaverse.listeners;

import java.util.Calendar;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroPlayer;

public class PlayerQuitListener implements Listener {
	VaroGame game = VaroGame.getInstance();
	VaroMessenger msg = VaroMessenger.getInstance();
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		System.out.println("PlayerQuitListener.onPlayerQuit()");
		
		Player p = e.getPlayer();
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());

		if (vp == null) {
			vp = new VaroPlayer(p.getUniqueId());
			vp.alive = game.state == GameState.IDLE;
			game.players.add(vp);
		}

		if (!vp.alive)
			return;

		if (Calendar.getInstance().getTimeInMillis() - vp.lastAttacked < 30000) {
			game.kill(p);
			msg.broadcast(p.getName() + " hat sich im Kampf ausgeloggt und ist damit ausgeschieden.");
		}
	}
}
