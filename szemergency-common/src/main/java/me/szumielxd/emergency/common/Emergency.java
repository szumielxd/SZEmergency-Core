package me.szumielxd.emergency.common;

import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.emergency.common.objects.CommonScheduler;
import me.szumielxd.emergency.common.objects.SenderWrapper;

public interface Emergency<T, U extends T> {
	
	
	public static final String SERVERLIST_CHANNEL = "psl:main";
	
	
	public @NotNull Logger getLogger();
	
	public @NotNull CommonScheduler getScheduler();
	
	public @NotNull SenderWrapper<T, U> getSenderWrapper();
	
	public @NotNull String getName();
	
	public @NotNull String getVersion();
	
	
	public void onEnable();
	
	public void onDisable();
	

}
