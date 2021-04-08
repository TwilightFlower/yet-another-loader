package io.github.nuclearfarts.yetanotherloader.impl.mod;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.nuclearfarts.yetanotherloader.api.mod.Mod;
import io.github.nuclearfarts.yetanotherloader.api.mod.ModProvider;
import io.github.nuclearfarts.yetanotherloader.api.plugin.YalPlugin;

public class DefaultModProvider implements ModProvider, YalPlugin {
	private List<ModImpl> mods;
	
	private void loadMods() {
		mods = new ArrayList<>();
		Path modsFolder = Paths.get("mods");
		List<Path> roots = new ArrayList<>();
		try {
			Files.createDirectories(modsFolder);
			for(Path p : Files.newDirectoryStream(modsFolder, Files::isRegularFile)) {
				FileSystem zipfs = FileSystems.newFileSystem(p, getClass().getClassLoader());
				Path fsRoot = zipfs.getPath("/");
				roots.add(p);
				mods.add(new ModImpl(fsRoot, zipfs));
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not scan mods folder", e);
		}
	}
	
	@Override
	public void exitCallback() {
		for(ModImpl mod : mods) {
			try {
				mod.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Collection<? extends Mod> getMods() {
		if(mods == null) {
			loadMods();
		}
		return mods;
	}
}
