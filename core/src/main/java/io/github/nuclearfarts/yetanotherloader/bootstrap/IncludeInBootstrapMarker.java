package io.github.nuclearfarts.yetanotherloader.bootstrap;

public interface IncludeInBootstrapMarker {
	default Class<?>[] getAdditionalClasses() {
		return new Class[0];
	}
	
	default String[] getAdditionalNames() {
		return new String[0];
	}
}
