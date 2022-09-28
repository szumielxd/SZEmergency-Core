package me.szumielxd.emergency.common.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.emergency.common.Emergency;
import me.szumielxd.emergency.common.configuration.Config;
import me.szumielxd.emergency.common.objects.SenderWrapper;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MainCommand<T> extends CommonCommand<T> {
	
	private final @NotNull Emergency<T, ?> plugin;

	public MainCommand(@NotNull Emergency<T, ?> plugin) {
		super(Config.COMMAND_NAME.getString(), "proxyserverlist.command.main", Config.COMMAND_ALIASES.getStringList().toArray(new String[0]));
		this.plugin = plugin;
	}

	@Override
	public void execute(@NotNull T sender, String[] args) {
		SenderWrapper<T, ?> swrapper = this.plugin.getSenderWrapper();
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				if (swrapper.hasPermission(sender, "proxyserverlist.command.reload")) {
					boolean failed = false;
					UnaryOperator<String> replacer = (str) -> {
						return str.replace("{plugin}", this.plugin.getName()).replace("{version}", this.plugin.getVersion());
					};
					swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(replacer.apply(Config.PREFIX.getString()+Config.COMMAND_SUB_RELOAD_EXECUTE.getString())));
					try {
						this.plugin.onDisable();
					} catch (Exception e) {
						e.printStackTrace();
						failed = true;
					}
					try {
						this.plugin.onEnable();
					} catch (Exception e) {
						e.printStackTrace();
						failed = true;
					}
					if (failed) swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(replacer.apply(Config.PREFIX.getString()+Config.COMMAND_SUB_RELOAD_ERROR.getString())));
					else swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(replacer.apply(Config.PREFIX.getString()+Config.COMMAND_SUB_RELOAD_SUCCESS.getString())));
				} else {
					swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.MESSAGES_PERM_ERROR.getString()));
				}
				return;
			} else if (args[0].equalsIgnoreCase("restart")) {
				if (swrapper.hasPermission(sender, "proxyserverlist.command.main.restart")) {
					if (args.length == 2) {
						Optional<SocketAddress> address = swrapper.getServerAddress(args[1]);
						if (address.isPresent()) {
							if (address.get() instanceof InetSocketAddress) {
								final InetSocketAddress inet = (InetSocketAddress) address.get();
								final String serverName = swrapper.getServerNames().stream()
										.filter(args[1]::equalsIgnoreCase).findAny()
										.orElseThrow(RuntimeException::new);
								new Thread(() -> {
									Exception ex = null;
									try (Socket sock = new Socket()) {
										byte[] password = Arrays.copyOf(Config.SEREVRRESTART_PASSWORD.getString().getBytes(StandardCharsets.US_ASCII), 64);
										sock.connect(new InetSocketAddress(inet.getHostString(), Config.SEREVRRESTART_PORT.getInt()));
										try (OutputStream out = sock.getOutputStream();
												InputStream in = sock.getInputStream()) {
											out.write(password);
											out.write(new byte[] { (byte) (inet.getPort() >> 8), (byte) inet.getPort() });
											if (in.read() == 0x00) throw new IllegalStateException(String.format("There is no process running on port `%s`", inet.getPort()));
										}
									} catch (IOException e) {
										ex = e;
										e.printStackTrace();
									} catch (IllegalStateException e) {
										ex = e;
									}
									if (ex == null) swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.COMMAND_SUB_RESTART_SUCCESS.getString().replace("{server}", serverName)));
									else swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.COMMAND_SUB_RESTART_ERROR.getString().replace("{server}", serverName).replace("{message}", ex.getLocalizedMessage())));
								}).start();
								return;
							} else {
								swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.MESSAGES_UNSUPPORTED_SERVER.getString()));
							}
						} else {
							swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.MESSAGES_INVALID_SERVER.getString()));
						}
					} else {
						swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.COMMAND_SUB_RESTART_USAGE.getString().replace("{cmd}", this.getName())));
					}
				} else {
					swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.MESSAGES_PERM_ERROR.getString()));
				}
			}
		}
		swrapper.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+"/"+Config.COMMAND_NAME.getString()+" reload|rl"));
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull T sender, @NotNull String[] args) {
		SenderWrapper<T, ?> swrapper = this.plugin.getSenderWrapper();
		ArrayList<String> list = new ArrayList<>();
		if (swrapper.hasPermission(sender, "proxyserverlist.command.main")) {
			if (args.length == 1) {
				String arg = args[0].toLowerCase();
				if ("reload".startsWith(arg) && swrapper.hasPermission(sender, "proxyserverlist.command.main.reload")) list.add("reload");
				if ("rl".startsWith(arg) && swrapper.hasPermission(sender, "proxyserverlist.command.main.reload")) list.add("rl");
				if ("restart".startsWith(arg) && swrapper.hasPermission(sender, "proxyserverlist.command.main.restart")) list.add("restart");
				return list;
			} else if (args.length == 2) {
				String arg = args[1].toLowerCase();
				if (args[0].equalsIgnoreCase("restart")) {
					swrapper.getServerNames().stream().filter(str -> str.toLowerCase().startsWith(arg)).forEach(list::add);
				}
			}
		}
		return list;
	}

}
