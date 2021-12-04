package net.kreaverse.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.kreaverse.model.VaroGame;
import net.kreaverse.model.VaroMessenger;

public class TeamCommands implements CommandExecutor{
	private VaroGame game;
	private VaroMessenger msg;
	
	public TeamCommands(VaroGame game, VaroMessenger msg) {
		this.game = game;
		this.msg = msg;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {

		return true;
	}
	
}
