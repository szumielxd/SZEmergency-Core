package me.szumielxd.emergency.bungee.commands;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.emergency.bungee.EmergencyBungee;
import me.szumielxd.emergency.common.commands.CommonCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommandWrapper extends Command implements TabExecutor {
	
	
	private final @NotNull EmergencyBungee plugin;
	private final @NotNull CommonCommand<CommandSender> command;
	

	public BungeeCommandWrapper(@NotNull EmergencyBungee plugin, @NotNull CommonCommand<CommandSender> command) {
		super(command.getName(), command.getPermission(), command.getAliases());
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.command = Objects.requireNonNull(command, "command cannot be null");
	}


	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		return this.command.onTabComplete(sender, args);
	}


	@Override
	public void execute(CommandSender sender, String[] args) {
		this.command.execute(sender, args);
	}

}
