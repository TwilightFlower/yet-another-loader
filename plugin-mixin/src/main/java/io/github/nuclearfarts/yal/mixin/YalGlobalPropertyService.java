package io.github.nuclearfarts.yal.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

public class YalGlobalPropertyService implements IGlobalPropertyService {
	private static final Map<IPropertyKey, Object> PROPERTIES = new HashMap<>();
	
	@Override
	public IPropertyKey resolveKey(String name) {
		return new PropertyKey(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(IPropertyKey key) {
		return (T) PROPERTIES.get(key);
	}

	@Override
	public void setProperty(IPropertyKey key, Object value) {
		PROPERTIES.put(key, value);
	}

	@Override
	public <T> T getProperty(IPropertyKey key, T defaultValue) {
		@SuppressWarnings("unchecked")
		T result = (T) PROPERTIES.get(key);
		return result != null ? result : defaultValue;
	}

	@Override
	public String getPropertyString(IPropertyKey key, String defaultValue) {
		Object result = PROPERTIES.get(key);
		return result != null ? result.toString() : defaultValue;
	}
	
	static final class PropertyKey implements IPropertyKey {
		final String str;
		PropertyKey(String str) {
			this.str = str;
		}
		
		@Override
		public int hashCode() {
			return str.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof PropertyKey)) return false;
			return str.equals(((PropertyKey) other).str);
		}
	}
}
