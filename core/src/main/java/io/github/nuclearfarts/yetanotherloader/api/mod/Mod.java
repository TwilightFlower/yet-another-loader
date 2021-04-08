package io.github.nuclearfarts.yetanotherloader.api.mod;

import java.nio.file.Path;
import java.util.Map;

public interface Mod {
	String getId();
	String getName();
	Path getFsRoot();
	Map<String, ?> getConfigMap();
}
