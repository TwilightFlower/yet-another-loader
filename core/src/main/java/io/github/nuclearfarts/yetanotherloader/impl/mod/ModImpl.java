package io.github.nuclearfarts.yetanotherloader.impl.mod;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import io.github.nuclearfarts.yetanotherloader.api.mod.Mod;
import io.github.nuclearfarts.yetanotherloader.impl.util.Util;

public class ModImpl implements Mod, Closeable {
	private final String id;
	private final String name;
	private final Map<String, ?> config;
	
	private final FileSystem modFs;
	private final Path fsRoot;
	
	public ModImpl(Path fsRoot, FileSystem fs) throws IOException {
		this.modFs = fs;
		this.fsRoot = fsRoot;
		TomlParseResult result = Toml.parse(fsRoot.resolve("mod.toml"));
		id = result.getString("id");
		name = result.getString("name");
		config = Util.detoml(result);
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Path getFsRoot() {
		return fsRoot;
	}

	@Override
	public void close() throws IOException {
		if(modFs != null) {
			modFs.close();
		}
	}

	@Override
	public Map<String, ?> getConfigMap() {
		return config;
	}
}
