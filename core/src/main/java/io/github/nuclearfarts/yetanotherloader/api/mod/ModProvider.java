package io.github.nuclearfarts.yetanotherloader.api.mod;

import java.util.Collection;

public interface ModProvider {
	Collection<? extends Mod> getMods();
}
