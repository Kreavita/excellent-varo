package net.kreaverse.listeners;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import net.kreaverse.ExcellentVARO;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroPlayer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcher.Item;

public class GlowHandler {
	/*
	 * Grüße gehen raus an Tom, der mir geholfen hat mit den Packets und dem
	 * obfuscateten NMS bullshit :)
	 */

	private VaroGame game;
	private ExcellentVARO plugin;

	public GlowHandler(VaroGame game, ExcellentVARO plugin) {
		this.game = game;
		this.plugin = plugin;
		initPacketListener();
	}

	public void sendUpdatePackets(Player target) { // trigger the glow listener by resending the last metadata packet
		Bukkit.getServer().getOnlinePlayers().forEach((player) -> {

			PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(player.getEntityId(),
					((CraftPlayer) player).getHandle().ai(), true); // deobf: getDataWatcher()

			((CraftPlayer) target).getHandle().b.a(metadataPacket); // deobf:
																	// playerConnection.sendPacket(metadataPacket)
		});
	}

	private void initPacketListener() {
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
					@Override
					public void onPacketSending(PacketEvent event) {
						PacketContainer packet = event.getPacket();

						if (!(packet.getType() == PacketType.Play.Server.ENTITY_METADATA))
							return;

						Player receiver = event.getPlayer();

						Entity entity = packet.getEntityModifier(receiver.getWorld()).read(0);

						if (!(entity instanceof Player))
							return;

						VaroPlayer glowTarget = game.getPlayerByUUID(((Player) entity).getUniqueId());

						boolean targetShouldGlow = !(glowTarget == null || glowTarget.getTeammate() == null
								|| !glowTarget.getTeammate().equals(receiver.getUniqueId()));

						if (!targetShouldGlow) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// Bukkit.getLogger().log(Level.INFO, targetShouldGlow + ": " + ((Player)
							// entity).getName()
							// + " -> " + receiver.getName());
						}

						List<DataWatcher.Item<?>> items = (List<Item<?>>) packet.getModifier().withType(List.class)
								.read(0);

						DataWatcher.Item item = items.get(0);

						if (item.b().getClass() != Byte.class)
							return;

						byte byteMask = (byte) item.b();
						item.a((byte) (targetShouldGlow ? (byteMask | 1 << 6) : (byteMask & ~(1 << 6))));
					}
				});
	}
}
