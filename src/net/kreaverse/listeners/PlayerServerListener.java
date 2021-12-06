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
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PlayerServerListener implements Listener {
	private VaroGame game;
	private VaroMessenger msg;

	public PlayerServerListener(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		VaroPlayer vp = game.playerJoin(e.getPlayer());
		e.joinMessage(e.joinMessage().append(Component.text((vp.alive) ? " (als Spieler)" : " (als Zuschauer)")));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		System.out.println("PlayerQuitListener.onPlayerQuit()");

		Player p = e.getPlayer();
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());

		if (vp == null || !vp.alive || game.paused || game.getState() != GameState.ONGOING)
			return;

		if (vp.lastAttacker != null) {
			msg.broadcast(p.getName() + " hat sich im Kampf ausgeloggt und ist damit ausgeschieden. Es verbleiben "
					+ (game.aliveCount - 1) + " Spieler.", ChatColor.RED);
			game.playerKill(p);
		}
	}

	@EventHandler
	public void onPlayerMessage(AsyncChatEvent e) {
		if (game.getState() != GameState.ONGOING)
			return;

		Player p = e.getPlayer();
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());

		if (vp == null || vp.alive)
			return;

		String message = PlainTextComponentSerializer.plainText().serialize(e.message());
		if (message.strip().charAt(0) != '/') {
			e.setCancelled(true);
			msg.playerError(e.getPlayer(), "Du darfst als Zuschauer nicht chatten.");
		}
	}
}
