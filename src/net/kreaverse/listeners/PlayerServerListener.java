package net.kreaverse.listeners;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
		Player p = e.getPlayer();
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());

		if (vp == null || !vp.alive || game.paused || game.getState() != GameState.ONGOING)
			return;

		if (vp.lastAttacker != null) {
			msg.broadcast(p.getName() + " hat sich im Kampf ausgeloggt und ist damit ausgeschieden. Es verbleiben "
					+ (game.aliveCount - 1) + " Spieler.", ChatColor.RED);
			game.playerKill(p);
		}

		if (!vp.alive || vp.getTeammate() == null)
			return;

		VaroPlayer vpTeammate = game.getPlayerByUUID(vp.getTeammate());

		if (vpTeammate == null || Bukkit.getPlayer(vp.getTeammate()) == null || vpTeammate.alive)
			return;

		Bukkit.getPlayer(vp.getTeammate()).kick(Component.text(ChatColor.RED
				+ "Du bist tot und kannst nicht länger zuschauen, da dein Teammate noch lebt und den Server verlassen hat."));

	}

	@EventHandler
	public void onPlayerMessage(AsyncChatEvent e) {
		e.setCancelled(true);

		if (game.paused)
			return;

		Player sender = e.getPlayer();
		VaroPlayer vpSender = game.getPlayerByUUID(sender.getUniqueId());

		if (game.getState() == GameState.ONGOING && (vpSender == null || !vpSender.alive)) {

			String message = PlainTextComponentSerializer.plainText().serialize(e.message());
			Bukkit.getLogger().log(Level.INFO, "Spectator Message: <" + sender.getName() + ">" + message);

			Bukkit.getOnlinePlayers().forEach(receiver -> {

				VaroPlayer vpReceiver = game.getPlayerByUUID(receiver.getUniqueId());

				if (vpReceiver != null && vpReceiver.alive) {
					return;
				}

				msg.spectatorMessage(sender, receiver, e.message());
			});
			return;
		}

		msg.chatMessage(sender, e.message());
	}

	private String[] pmPrefixes = { "/msg ", "/tell ", "/r ", "/whisper ", "/w ", "/me " };

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {

		VaroPlayer vp = game.getPlayerByUUID(e.getPlayer().getUniqueId());

		if (e.getPlayer().isOp() || vp != null && vp.alive) {
			return;
		}

		for (String s : pmPrefixes) {
			if (e.getMessage().strip().startsWith(s)) {
				e.setCancelled(true);
				msg.playerError(e.getPlayer(), "Du bist tot und darfst diesen Command nicht nutzen!");
				return;
			}
		}
	}

}
