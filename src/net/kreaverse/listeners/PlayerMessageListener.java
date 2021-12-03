package net.kreaverse.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PlayerMessageListener implements Listener{
	VaroGame game = VaroGame.getInstance();
	VaroMessenger msg = VaroMessenger.getInstance();

	@EventHandler
	public void onPlayerMessage(AsyncChatEvent e) {
//		System.out.println("PlayerMessageListener.onPlayerMessage()");
		String message = PlainTextComponentSerializer.plainText().serialize(e.message());
		Player p = e.getPlayer();
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());
		e.message().toString();

		if (game.state == GameState.ONGOING && (vp == null || !vp.alive && message.strip().charAt(0) != '/')) {
			e.setCancelled(true);
			msg.playerError(e.getPlayer(), "Du darfst als Zuschauer nicht chatten.");
		}

		game.updatePlayer(p);
	}

}
