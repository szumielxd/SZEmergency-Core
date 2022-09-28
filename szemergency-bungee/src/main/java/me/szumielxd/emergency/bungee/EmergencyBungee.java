package me.szumielxd.emergency.bungee;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.emergency.bungee.commands.BungeeCommandWrapper;
import me.szumielxd.emergency.bungee.objects.BungeeScheduler;
import me.szumielxd.emergency.bungee.objects.BungeeSenderWrapper;
import me.szumielxd.emergency.common.Emergency;
import me.szumielxd.emergency.common.EmergencyProvider;
import me.szumielxd.emergency.common.commands.CommonCommand;
import me.szumielxd.emergency.common.commands.MainCommand;
import me.szumielxd.emergency.common.configuration.Config;
import me.szumielxd.emergency.common.objects.CommonScheduler;
import me.szumielxd.emergency.common.objects.SenderWrapper;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class EmergencyBungee extends Plugin implements Emergency<CommandSender, ProxiedPlayer> {
	
	
	private BungeeAudiences adventure = null;
	private @Nullable BungeeScheduler scheduler;
	private @Nullable BungeeSenderWrapper senderWrapper;
	
	
	
	public BungeeAudiences adventure() {
		if (this.adventure == null) throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
		return this.adventure;
	}
	
	
	@Override
	public void onEnable() {
		EmergencyProvider.init(this);
		this.scheduler = new BungeeScheduler(this);
		this.adventure = BungeeAudiences.create(this);
		this.senderWrapper = new BungeeSenderWrapper(this);
		this.registerCommand(new MainCommand<>(this));
		Config.load(new File(this.getDataFolder(), "config.yml"), this);
	}
	
	
	private void registerCommand(@NotNull CommonCommand<CommandSender> command) {
		this.getProxy().getPluginManager().registerCommand(this, new BungeeCommandWrapper(this, command));
	}
	
	
	@Override
	public void onDisable() {
		this.getLogger().info("Disabling all modules...");
		this.getProxy().getScheduler().cancel(this);
		this.getProxy().getPluginManager().unregisterListeners(this);
		if (this.adventure != null) {
			this.adventure.close();
			this.adventure = null;
		}
		try {
			Class<?> bungeeAudiencesImpl = Class.forName("net.kyori.adventure.platform.bungeecord.BungeeAudiencesImpl");
			Field f = bungeeAudiencesImpl.getDeclaredField("INSTANCES");
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, BungeeAudiences> instances = (Map<String, BungeeAudiences>) f.get(null);
			instances.remove(this.getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.getProxy().getPluginManager().unregisterCommands(this);
		this.getLogger().info("Well done. Time to sleep!");
	}


	@Override
	public @NotNull CommonScheduler getScheduler() {
		if (this.scheduler == null) throw new IllegalStateException("Plugin is not initialized");
		return this.scheduler;
	}


	@Override
	public @NotNull SenderWrapper<CommandSender, ProxiedPlayer> getSenderWrapper() {
		if (this.senderWrapper == null) throw new IllegalStateException("Plugin is not initialized");
		return this.senderWrapper;
	}


	@Override
	public @NotNull String getName() {
		return this.getDescription().getName();
	}


	@Override
	public @NotNull String getVersion() {
		return this.getDescription().getVersion();
	}
	

}
