package me.szumielxd.emergency.velocity;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import me.szumielxd.emergency.common.Emergency;
import me.szumielxd.emergency.common.EmergencyProvider;
import me.szumielxd.emergency.common.commands.CommonCommand;
import me.szumielxd.emergency.common.commands.MainCommand;
import me.szumielxd.emergency.common.configuration.Config;
import me.szumielxd.emergency.common.objects.CommonScheduler;
import me.szumielxd.emergency.velocity.commands.VelocityCommandWrapper;
import me.szumielxd.emergency.velocity.objects.VelocityScheduler;
import me.szumielxd.emergency.velocity.objects.VelocitySenderWrapper;

@Plugin(
		id = "id----",
		name = "@pluginName@",
		version = "@version@",
		authors = { "@author@" },
		description = "@description@",
		url = "https://github.com/szumielxd/ProxyServerList/",
		dependencies = { 
				@Dependency( id="protocolize", optional=false )
		}
)
public class EmergencyVelocity implements Emergency<CommandSource, Player> {
	
	
	private final ProxyServer server;
	private final Logger logger;
	private final File dataFolder;
	
	
	private @Nullable VelocityScheduler scheduler;
	private @Nullable VelocitySenderWrapper senderWrapper;
	
	
	@Inject
	public EmergencyVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		this.server = server;
		this.logger = logger;
		this.dataFolder = dataDirectory.toFile();
	}
	
	
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
	    this.onEnable();
	}
	
	
	@Override
	public void onEnable() {
		EmergencyProvider.init(this);
		this.scheduler = new VelocityScheduler(this);
		this.senderWrapper = new VelocitySenderWrapper(this);
		this.registerCommand(new MainCommand<>(this));
		Config.load(new File(this.dataFolder, "config.yml"), this);
	}
	
	
	private void registerCommand(@NotNull CommonCommand<CommandSource> command) {
		CommandManager mgr = this.getProxy().getCommandManager();
		CommandMeta meta = mgr.metaBuilder(command.getName()).aliases(command.getAliases()).build();
		mgr.register(meta, new VelocityCommandWrapper(this, command));
	}
	
	
	@Override
	public void onDisable() {
		this.getLogger().info("Disabling all modules...");
		this.getProxy().getEventManager().unregisterListeners(this);
		this.getScheduler().cancelAll();
		this.getLogger().info("Well done. Time to sleep!");
	}
	
	
	@Override
	public @NotNull Logger getLogger() {
		return this.logger;
	}
	
	
	public @NotNull ProxyServer getProxy() {
		return this.server;
	}


	@Override
	public @NotNull CommonScheduler getScheduler() {
		if (this.scheduler == null) throw new IllegalStateException("Plugin is not initialized");
		return this.scheduler;
	}


	@Override
	public @NotNull VelocitySenderWrapper getSenderWrapper() {
		if (this.senderWrapper == null) throw new IllegalStateException("Plugin is not initialized");
		return this.senderWrapper;
	}


	@Override
	public @NotNull String getName() {
		return this.getProxy().getPluginManager().ensurePluginContainer(this).getDescription().getName().orElse("");
	}


	@Override
	public @NotNull String getVersion() {
		return this.getProxy().getPluginManager().ensurePluginContainer(this).getDescription().getVersion().orElse("");
	}
	

}
