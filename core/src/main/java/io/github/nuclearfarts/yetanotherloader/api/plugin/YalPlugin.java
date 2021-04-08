package io.github.nuclearfarts.yetanotherloader.api.plugin;

public interface YalPlugin {
	default void pluginLoadCallback() {}
	default void modTransformersLoadedCallback(ClassLoader loader) {}
	default void gameLoadedCallback(ClassLoader loader) {}
	default void exitCallback() {}
}
