package io.github.nuclearfarts.yal.devprovider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import io.github.nuclearfarts.yetanotherloader.api.mod.Mod;
import io.github.nuclearfarts.yetanotherloader.impl.util.Util;

class DevMod implements Mod {
	private final String id;
	private final String name;
	private final Map<String, ?> config;
	
	private final Path fsRoot;

	DevMod(InputStream toml, Path fsRoot) throws IOException {
		TomlParseResult result = Toml.parse(fsRoot.resolve("mod.toml"));
		id = result.getString("id");
		name = result.getString("name");
		config = Util.detoml(result);
		this.fsRoot = fsRoot;
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
	public Map<String, ?> getConfigMap() {
		return config;
	}

}
