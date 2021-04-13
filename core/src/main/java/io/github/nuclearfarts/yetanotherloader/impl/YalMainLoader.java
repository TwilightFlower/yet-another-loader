package io.github.nuclearfarts.yetanotherloader.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.github.nuclearfarts.yetanotherloader.api.ApiLoader;
import io.github.nuclearfarts.yetanotherloader.api.GameProvider;
import io.github.nuclearfarts.yetanotherloader.api.YalApi;
import io.github.nuclearfarts.yetanotherloader.api.mod.Mod;
import io.github.nuclearfarts.yetanotherloader.api.mod.ModProvider;
import io.github.nuclearfarts.yetanotherloader.api.plugin.YalPlugin;
import io.github.nuclearfarts.yetanotherloader.api.transformer.YalTransformer;
import io.github.nuclearfarts.yetanotherloader.impl.transformer.MainTransformer;
import io.github.nuclearfarts.yetanotherloader.impl.transformer.TransformingNioClassLoader;
import io.github.nuclearfarts.yetanotherloader.impl.util.DoubleDelegateClassLoader;
import io.github.nuclearfarts.yetanotherloader.impl.util.NioClassLoader;

public class YalMainLoader implements YalApi {
	public static final YalMainLoader INSTANCE = new YalMainLoader();
	
	private ClassLoader pluginLoader;
	private ClassLoader transformerLoader;
	private TransformingNioClassLoader gameLoader;
	private List<YalPlugin> plugins;
	private List<Path> modFsRoots;
	private final Map<String, Mod> mods = new HashMap<>();
	
	public static void loadProcess(Path pluginsDir, String[] args, ClassLoader originalLoader) throws IOException {
		INSTANCE.beginLoad(pluginsDir);
		INSTANCE.loadModTransformers(originalLoader);
		INSTANCE.loadGame(args, originalLoader);
	}
	
	public void beginLoad(Path pluginsDir) throws IOException {
		List<URL> urls = new ArrayList<>();
		Files.createDirectories(pluginsDir);
		for(Path p : Files.newDirectoryStream(pluginsDir, Files::isRegularFile)) {
			System.out.println("Found plugin " + p);
			urls.add(p.toUri().toURL());
		}
		pluginLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
		ApiLoader.loadSingleUnchecked(YalTransformer.class, MainTransformer.class, pluginLoader);
		ApiLoader.loadSingleUnchecked(GameProvider.class, pluginLoader);
		plugins = ApiLoader.loadServiceProviders(YalPlugin.class, pluginLoader);
		for(YalPlugin p : plugins) {
			p.pluginLoadCallback();
		}
	}
	
	public void loadModTransformers(ClassLoader originalLoader) {
		List<ModProvider> modProviders = ApiLoader.loadServiceProviders(ModProvider.class, pluginLoader);
		modFsRoots = new ArrayList<>();
		for(ModProvider p : modProviders) {
			Collection<? extends Mod> ms = p.getMods();
			for(Mod m : ms) {
				if(!mods.containsKey(m.getId())) {
					mods.put(m.getId(), m);
					modFsRoots.add(m.getFsRoot());
				} else {
					System.err.println("Duplicate mod id: " + m.getId());
				}
			}
		}
		transformerLoader = new NioClassLoader(modFsRoots, new DoubleDelegateClassLoader(pluginLoader, originalLoader));
		for(YalPlugin p : plugins) {
			p.modTransformersLoadedCallback(transformerLoader);
		}
	}
	
	public void loadGame(String[] args, ClassLoader originalLoader) {
		GameProvider gp = GameProvider.get();
		Collection<Path> gameFsRoots = gp.getFsRoots();
		gameFsRoots.addAll(modFsRoots);
		gameLoader = new TransformingNioClassLoader(gameFsRoots, pluginLoader, originalLoader);
		for(YalPlugin p : plugins) {
			p.gameLoadedCallback(gameLoader);
		}
		gp.launch(gameLoader, args);
	}

	@Override
	public boolean isModLoaded(String id) {
		return mods.containsKey(id);
	}
	
	@Override
	public Collection<? extends Mod> getLoadedMods() {
		return mods.values();
	}

	@Override
	public ClassLoader getPluginClassLoader() {
		return pluginLoader;
	}

	@Override
	public ClassLoader getTransformerClassLoader() {
		return transformerLoader;
	}

	@Override
	public boolean isOnTransformationPath(String resLoc) {
		return gameLoader.findResource(resLoc) != null;
	}
}
