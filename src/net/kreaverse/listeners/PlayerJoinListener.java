package net.kreaverse.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroPlayer;
import net.kreaverse.model.VaroGame.GameState;

public class PlayerJoinListener implements Listener {

	VaroGame game = VaroGame.getInstance();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {

		Player p = e.getPlayer();
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());

		if (vp == null) {
			vp = new VaroPlayer(p.getUniqueId());
			vp.alive = game.state == GameState.IDLE;
			game.players.add(vp);
		}

		game.updatePlayer(p);
	}
}
