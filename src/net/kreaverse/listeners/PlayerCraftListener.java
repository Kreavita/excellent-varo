package net.kreaverse.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import net.kreaverse.ExcellentVARO;
import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroGame.GameState;
import net.kreaverse.model.VaroMessenger;
import net.kreaverse.model.VaroPlayer;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class PlayerCraftListener implements Listener {
	private VaroGame game;
	private VaroMessenger msg;

	private ShapelessRecipe goldAppleRevive;
	private ShapelessRecipe witherSkullRevive;

	public PlayerCraftListener(VaroGame game, VaroMessenger msg, ExcellentVARO plugin) {
		this.game = game;
		this.msg = msg;

		ItemStack item = new ItemStack(Material.DRAGON_EGG);
		ItemMeta meta = item.getItemMeta();

		meta.displayName(Component.text(ChatColor.GOLD + "Teammate Wiederbeleben"));
		item.addUnsafeEnchantment(Enchantment.MENDING, 10);
		item.setItemMeta(meta);

		goldAppleRevive = new ShapelessRecipe(new NamespacedKey(plugin, "goldAppleRevive"), item);
		goldAppleRevive.addIngredient(Material.ENCHANTED_GOLDEN_APPLE);
		plugin.getServer().addRecipe(goldAppleRevive);

		witherSkullRevive = new ShapelessRecipe(new NamespacedKey(plugin, "witherSkullRevive"), item);
		witherSkullRevive.addIngredient(Material.WITHER_SKELETON_SKULL);
		plugin.getServer().addRecipe(witherSkullRevive);

	}

	@EventHandler
	public void onPlayerCraft(CraftItemEvent e) {
		if (!e.getRecipe().getResult().getType().equals(Material.DRAGON_EGG)) {
			return;
		}

		e.setCancelled(true);

		if (game.getState() != GameState.ONGOING) {
			return;
		}

		Player p = (Player) e.getWhoClicked();
		VaroPlayer vp = game.getPlayerByUUID(p.getUniqueId());

		if (vp == null || vp.getTeammate() == null) {
			msg.playerError(p, "Du hast keinen Mitspieler.");
			return;
		}

		VaroPlayer vpTeammate = game.getPlayerByUUID(vp.getTeammate());

		if (vpTeammate == null) {
			msg.playerError(p, "Dein Mitspieler ist ungültig (Serverfehler).");
			return;
		}

		if (vpTeammate.alive) {
			msg.playerError(p, "Dein Mitspieler kann nicht wiederbelebt werden, da er bereits lebt.");
			return;
		}
		if (vpTeammate.revivesLeft == 0) {
			msg.playerError(p, "Dein Mitspieler kann nicht mehr wiederbelebt werden.");
			return;
		}

		Player teammate = Bukkit.getPlayer(vpTeammate.player);

		if (teammate == null) {
			msg.playerError(p, "Dein Mitspieler kann nicht wiederbelebt werden, da er nicht online ist.");
			return;
		}

		teammate.teleport(p);
		game.updateTeamGlow(teammate, p);

		game.playerRevive(teammate);
		vpTeammate.revivesLeft--;

		e.getInventory().clear();
		e.getClickedInventory().clear();

		msg.playerSuccess(p, "Dein Mitspieler wurde wiederbelebt.");
	}
}
