package me.szumielxd.emergency.common.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.MemorySection;
import org.simpleyaml.configuration.file.YamlConfiguration;

import me.szumielxd.emergency.common.Emergency;
import me.szumielxd.emergency.common.utils.MiscUtil;

public enum Config {
	
	PREFIX("common.prefix", "&4&lEmergency&r &8&l»&r &7", true),
	DEBUG("common.debug", false),
	SEREVRRESTART_PORT("server-restart.port", 7176),
	SEREVRRESTART_PASSWORD("server-restart.password", "MY_PASSWORD"),
	MESSAGES_PERM_ERROR("message.perm-error", "&cNo, you can't", true),
	MESSAGES_COMMAND_ERROR("message.command-error", "&4An error occured while attempting to perform this command. Please report this to admin.", true),
	MESSAGES_CONSOLE_ERROR("message.console-error", "&cNot for console ;c", true),
	MESSAGES_INVALID_SERVER("message.invalid-server", "&cInvalid server", true),
	MESSAGES_UNSUPPORTED_SERVER("message.unsupported-server", "&cUnsupported server address", true),
	COMMAND_NAME("command.name", "emergency"),
	COMMAND_ALIASES("command.aliases", Arrays.asList()),
	COMMAND_SUB_RELOAD_EXECUTE("command.sub.reload.execute", "Reloading...", true),
	COMMAND_SUB_RELOAD_ERROR("command.sub.reload.error", "&cAn error occured while reloading plugin. See console for more info.", true),
	COMMAND_SUB_RELOAD_SUCCESS("command.sub.reload.success", "&aSuccessfully reloaded {plugin} v{version}", true),
	COMMAND_SUB_RESTART_USAGE("command.sub.restart.usage", "&cUse &b/{cmd} restart <server>", true),
	COMMAND_SUB_RESTART_ERROR("command.sub.restart.error", "&cAn error occured while restarting server &b{server}&c: &4{message}&c.", true),
	COMMAND_SUB_RESTART_SUCCESS("command.sub.restart.success", "&aSuccessfully restarted server &b{server}&a.", true),
	;
	
	
	
	//////////////////////////////////////////////////////////////////////
	
	private final String path;
	private List<String> texts;
	private String text;
	private int number;
	private boolean bool;
	private Map<String, Object> map;
	private boolean colored = false;
	private Class<?> type;
	
	
	private Config(String path, String text) {
		this(path, text, false);
	}
	private Config(String path, String text, boolean colored) {
		this.path = path;
		this.colored = colored;
		setValue(text);
	}
	private Config(String path, List<String> texts) {
		this(path, texts, false);
	}
	private Config(String path, List<String> texts, boolean colored) {
		this.path = path;
		this.colored = colored;
		setValue(texts);
	}
	private Config(String path, int number) {
		this.path = path;
		setValue(number);
	}
	private Config(String path, boolean bool) {
		this.path = path;
		setValue(bool);
	}
	private Config(String path, Map<String, Object> valueMap) {
		this.path = path;
		setValue(valueMap);
	}
	
	
	
	//////////////////////////////////////////////////////////////////////
	
	private void setValue(String text) {
		this.type = String.class;
		this.text = text;
		this.texts = new ArrayList<>(Arrays.asList(this.text));
		this.number = text.length();
		this.bool = !text.isEmpty();
		this.map = new HashMap<>();
	}
	private void setValue(List<String> texts) {
		this.type = String[].class;
		this.text = String.join(", ", texts);
		this.texts = texts;
		this.number = texts.size();
		this.bool = !texts.isEmpty();
		AtomicInteger index = new AtomicInteger(0);
		this.map = texts.stream().collect(Collectors.toMap(v -> String.valueOf(index.getAndAdd(1)), v -> v));
	}
	private void setValue(int number) {
		this.type = Integer.class;
		this.text = Integer.toString(number);
		this.texts = new ArrayList<>(Arrays.asList(this.text));
		this.number = number;
		this.bool = number > 0;
		this.map = new HashMap<>();
	}
	private void setValue(boolean bool) {
		this.type = Boolean.class;
		this.text = Boolean.toString(bool);
		this.texts = new ArrayList<>(Arrays.asList(this.text));
		this.number = bool? 1 : 0;
		this.bool = bool;
		this.map = new HashMap<>();
	}
	private void setValue(Map<String, Object> valueMap) {
		this.type = Map.class;
		this.text = valueMap.toString();
		this.texts = valueMap.values().stream().map(Object::toString).collect(Collectors.toList());
		this.number = valueMap.size();
		this.bool = !valueMap.isEmpty();
		this.map = valueMap;
	}
	
	
	public String getString() {
		return this.text;
	}
	@Override
	public String toString() {
		return this.text;
	}
	public List<String> getStringList() {
		return new ArrayList<>(this.texts);
	}
	public int getInt() {
		return this.number;
	}
	public boolean getBoolean() {
		return this.bool;
	}
	public Map<String, Object> getValueMap() {
		return this.map;
	}
	public boolean isColored() {
		return this.colored;
	}
	public Class<?> getType() {
		return this.type;
	}
	public String getPath() {
		return this.path;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////
	
	public static void load(@NotNull File file, @NotNull Emergency<?, ?> plugin) {
		Objects.requireNonNull(plugin, "plugin cannot be null").getLogger().info("Loading configuration from '" + Objects.requireNonNull(file, "file cannot be null").getName() + "'");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		try {
			if(!file.exists()) file.createNewFile();
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			if(loadConfig(config) > 0) config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static int loadConfig(Configuration config) {
		int modify = 0;
		for (Config val : Config.values()) {
			if(!config.contains(val.getPath())) modify++;
			if (val.getType().equals(String.class)) {
				if (val.isColored())val.setValue(getColoredStringOrSetDefault(config, val.getPath(), val.getString()));
				else val.setValue(getStringOrSetDefault(config, val.getPath(), val.getString()));
			} else if (val.getType().equals(String[].class)) {
				if (val.isColored())val.setValue(getColoredStringListOrSetDefault(config, val.getPath(), val.getStringList()));
				else val.setValue(getStringListOrSetDefault(config, val.getPath(), val.getStringList()));
			} else if (val.getType().equals(Integer.class)) val.setValue(getIntOrSetDefault(config, val.getPath(), val.getInt()));
			else if (val.getType().equals(Boolean.class)) val.setValue(getBooleanOrSetDefault(config, val.getPath(), val.getBoolean()));
			else if (val.getType().equals(Map.class)) val.setValue(getMapOrSetDefault(config, val.getPath(), val.getValueMap()));
		}
		return modify;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Map<String,T> getMapOrSetDefault(Configuration config, String path, Map<String,T> def) {
		if (config.contains(path)) {
			return (Map<String, T>) ((MemorySection) config.getConfigurationSection(path)).getMapValues(false);
		}
		config.set(path, def);
		return def;
	}
	
	private static int getIntOrSetDefault(Configuration config, String path, int def) {
		if (config.contains(path)) return config.getInt(path);
		config.set(path, def);
		return def;
	}
	
	private static boolean getBooleanOrSetDefault(Configuration config, String path, boolean def) {
		if (config.contains(path)) return config.getBoolean(path);
		config.set(path, def);
		return def;
	}
	
	private static String getStringOrSetDefault(Configuration config, String path, String def) {
		if (config.contains(path)) return config.getString(path);
		config.set(path, def);
		return def;
	}
	
	private static String getColoredStringOrSetDefault(Configuration config, String path, String def) {
		return MiscUtil.translateAlternateColorCodes('&', getStringOrSetDefault(config, path, def.replace('§', '&')));
	}
	
	private static ArrayList<String> getStringListOrSetDefault(Configuration config, String path, List<String> def) {
		if(config.contains(path)) return new ArrayList<>(config.getStringList(path));
		config.set(path, def);
		return new ArrayList<>(def);
	}
	
	private static ArrayList<String> getColoredStringListOrSetDefault(Configuration config, String path, List<String> def) {
		ArrayList<String> list = getStringListOrSetDefault(config, path, def.stream().map(str -> str.replace('§', '&')).collect(Collectors.toCollection(ArrayList::new)));
		return list.stream().map((str) -> MiscUtil.translateAlternateColorCodes('&', str))
				.collect(Collectors.toCollection(ArrayList::new));
	}

}
