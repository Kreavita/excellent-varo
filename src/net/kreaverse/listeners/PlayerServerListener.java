package net.kreaverse.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;

public class PlayerServerListener implements Listener {
	private VaroGame game;
	private VaroMessenger msg;

	public PlayerServerListener(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

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

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		System.out.println("PlayerQuitListener.onPlayerQuit()");

		Player p = e.getPlayer();
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());

		if (vp == null || !vp.alive)
			return;

		if (vp.lastAttacker != null) {
			msg.broadcast(p.getName() + " hat sich im Kampf ausgeloggt und ist damit ausgeschieden.", ChatColor.RED);
			game.kill(p);
		}
	}

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
	}
}
