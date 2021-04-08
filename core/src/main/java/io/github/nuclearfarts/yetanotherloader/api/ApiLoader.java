package io.github.nuclearfarts.yetanotherloader.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public final class ApiLoader {
	private ApiLoader() {}
	private static final Map<Class<?>, Object> LOADED_APIS = new HashMap<>();
	private static final Map<Class<?>, Object> LOADED_SERVICES = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T> T getLoadedSingle(Class<T> clazz) {
		return (T) LOADED_APIS.get(clazz);
	}
	
	public static final <T> List<T> loadServiceProviders(Class<T> clazz, ClassLoader loader) {
		List<T> result = new ArrayList<>();
		ServiceLoader<T> sLoader = ServiceLoader.load(clazz, loader);
		for(T svc : sLoader) {
			svc = ensureSingleInstance(svc);
			result.add(svc);
		}
		return result;
	}
	
	public static <T> T loadSingle(Class<T> clazz) throws ApiProviderNotFoundException, MultipleApiProvidersException {
		return loadSingle(clazz, clazz.getClassLoader());
	}
	
	public static <T> T loadSingleUnchecked(Class<T> clazz) {
		try {
			return loadSingle(clazz);
		} catch(ApiProviderNotFoundException | MultipleApiProvidersException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T loadSingle(Class<T> clazz, ClassLoader loader) throws ApiProviderNotFoundException, MultipleApiProvidersException {
		return loadSingle(clazz, null, loader);
	}
	
	public static <T> T loadSingleUnchecked(Class<T> clazz, ClassLoader loader) {
		try {
			return loadSingle(clazz, loader);
		} catch(ApiProviderNotFoundException | MultipleApiProvidersException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T loadSingle(Class<T> clazz, Class<? extends T> defaultProvider, ClassLoader loader) throws ApiProviderNotFoundException, MultipleApiProvidersException {
		ServiceLoader<T> sLoader = ServiceLoader.load(clazz, loader);
		T providedDefault = null;
		List<T> providers = new ArrayList<>();
		for(T svc : sLoader) {
			svc = ensureSingleInstance(svc);
			if(svc.getClass() != defaultProvider) {
				providers.add(svc);
			} else {
				providedDefault = svc;
			}
		}
		if(providers.isEmpty()) {
			if(providedDefault == null) {
				throw new ApiProviderNotFoundException(clazz);
			} else {
				LOADED_APIS.put(clazz, providedDefault);
				return providedDefault;
			}
		}
		if(providers.size() > 1) {
			throw new MultipleApiProvidersException(clazz, providers);
		}
		LOADED_APIS.put(clazz, providers.get(0));
		return providers.get(0);
	}
	
	public static <T> T loadSingleUnchecked(Class<T> clazz, Class<? extends T> defaultProvider, ClassLoader loader) {
		try {
			return loadSingle(clazz, defaultProvider, loader);
		} catch(ApiProviderNotFoundException | MultipleApiProvidersException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T loadSingle(Class<T> clazz, Class<? extends T> defaultProvider) throws ApiProviderNotFoundException, MultipleApiProvidersException {
		return loadSingle(clazz, defaultProvider, clazz.getClassLoader());
	}
	
	public static <T> T loadSingleUnchecked(Class<T> clazz, Class<? extends T> defaultProvider) {
		try {
			return loadSingle(clazz, defaultProvider);
		} catch(MultipleApiProvidersException | ApiProviderNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T ensureSingleInstance(T t) {
		Class<?> clazz = t.getClass();
		if(LOADED_SERVICES.containsKey(clazz)) {
			t = (T) LOADED_SERVICES.get(clazz);
		} else {
			LOADED_SERVICES.put(clazz, t);
		}
		return t;
	}
	
	@SuppressWarnings("serial")
	public static class ApiProviderNotFoundException extends Exception {
		private final Class<?> api;
		
		private ApiProviderNotFoundException(Class<?> api) {
			super("No service providers found for " + api.getName());
			this.api = api;
		}
		
		public Class<?> getApiClass() {
			return api;
		}
	}
	
	@SuppressWarnings("serial")
	public static class MultipleApiProvidersException extends Exception {
		private final List<?> found;
		private final Class<?> api;
		
		private <T> MultipleApiProvidersException(Class<T> api, List<T> found) {
			super(String.format("Expected single provider for %s, found %d: %s", api.getName(), found.size(), found.toString()));
			this.found = found;
			this.api = api;
		}
		
		public List<?> getFoundProviders() {
			return found;
		}
		
		public Class<?> getApiClass() {
			return api;
		}
	}
}
