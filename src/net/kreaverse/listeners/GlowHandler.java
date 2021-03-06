package net.kreaverse.listeners;

import java.util.List;

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

	public void initiateTeamGlow(Player target, Player receiver) { // trigger the glow listener by resending the last
																	// metadata packet
		PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(target.getEntityId(),
				((CraftPlayer) target).getHandle().ai(), true); // deobf: getDataWatcher()

		((CraftPlayer) receiver).getHandle().b.a(metadataPacket); // deobf:
																	// playerConnection.sendPacket(metadataPacket)
	}

	private void initPacketListener() {
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(plugin, ListenerPriority.LOWEST, PacketType.Play.Server.ENTITY_METADATA) {
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

						if (targetShouldGlow) {
//							Bukkit.getLogger().log(Level.INFO, targetShouldGlow + ": " + ((Player) entity).getName()
//									+ " -> " + receiver.getName());
						}
						

						packet = packet.deepClone();
						event.setPacket(packet);

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
