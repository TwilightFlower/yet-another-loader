package io.github.nuclearfarts.yal.mixin;

import io.github.nuclearfarts.yal.mixin.api.SideNameProvider;

public class DefaultSideNameProvider implements SideNameProvider {
	@Override
	public String getSideName() {
		return "universal";
	}
}
