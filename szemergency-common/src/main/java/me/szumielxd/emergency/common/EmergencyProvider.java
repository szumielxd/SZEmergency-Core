package me.szumielxd.emergency.common;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmergencyProvider {
	
	
	private static @Nullable Emergency<?, ?> instance = null;
	
	
	public static void init(@NotNull Emergency<?, ?> instance) {
		EmergencyProvider.instance = Objects.requireNonNull(instance, "instance cannot be null");
	}
	
	
	public static @NotNull Emergency<?, ?> get() {
		if (instance == null) throw new IllegalArgumentException("ProxyAnnouncements is not initialized");
		return instance;
	}
	

}
