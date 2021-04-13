package io.github.nuclearfarts.yal.mixin;

import org.spongepowered.asm.service.IMixinService;

import io.github.nuclearfarts.yetanotherloader.bootstrap.IncludeInBootstrapMarker;

public class BootstrapMarker implements IncludeInBootstrapMarker {
	@Override
	public Class<?>[] getAdditionalClasses() {
		return new Class[] {
			IMixinService.class
		};
	}
	
	@Override
	public String[] getAdditionalNames() {
		return new String[] {
			"com.google.gson.Gson",
			"com.google.common.base.Objects"
		};
	}
}
