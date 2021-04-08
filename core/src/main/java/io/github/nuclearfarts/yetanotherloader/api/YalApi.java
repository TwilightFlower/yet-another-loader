package io.github.nuclearfarts.yetanotherloader.api;

import java.util.Collection;
import io.github.nuclearfarts.yetanotherloader.api.mod.Mod;
import io.github.nuclearfarts.yetanotherloader.impl.YalMainLoader;

public interface YalApi {
	static YalApi INSTANCE = YalMainLoader.INSTANCE;
	
	boolean isModLoaded(String id);
	Collection<? extends Mod> getLoadedMods();
	ClassLoader getPluginClassLoader();
	ClassLoader getTransformerClassLoader();
	boolean isOnTransformationPath(String resLoc);
}
