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
import org.bukkit.inventory.ShapedRecipe;
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
	private ShapedRecipe opApple;

	public PlayerCraftListener(VaroGame game, VaroMessenger msg, ExcellentVARO plugin) {
		this.game = game;
		this.msg = msg;

		ItemStack egg = new ItemStack(Material.DRAGON_EGG);
		ItemMeta eggMeta = egg.getItemMeta();

		eggMeta.displayName(Component.text(ChatColor.GOLD + "Teammate Wiederbeleben"));
		egg.addUnsafeEnchantment(Enchantment.MENDING, 10);
		egg.setItemMeta(eggMeta);

		goldAppleRevive = new ShapelessRecipe(new NamespacedKey(plugin, "goldAppleRevive"), egg);
		goldAppleRevive.addIngredient(Material.ENCHANTED_GOLDEN_APPLE);
		plugin.getServer().addRecipe(goldAppleRevive);

		witherSkullRevive = new ShapelessRecipe(new NamespacedKey(plugin, "witherSkullRevive"), egg);
		witherSkullRevive.addIngredient(Material.WITHER_SKELETON_SKULL);
		plugin.getServer().addRecipe(witherSkullRevive);
		
		opApple = new ShapedRecipe(new NamespacedKey(plugin, "enchantedGoldenApple"), new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
		opApple.shape("***","*o*","***");
		opApple.setIngredient('*', new ItemStack(Material.GOLD_BLOCK));
		opApple.setIngredient('o', new ItemStack(Material.APPLE));
		plugin.getServer().addRecipe(opApple);

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
