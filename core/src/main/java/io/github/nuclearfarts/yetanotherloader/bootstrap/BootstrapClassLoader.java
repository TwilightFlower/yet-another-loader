package io.github.nuclearfarts.yetanotherloader.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Predicate;

import io.github.nuclearfarts.yetanotherloader.impl.util.Util;

public class BootstrapClassLoader extends URLClassLoader {
	private final Predicate<String> allow;
	public BootstrapClassLoader(URL[] urls, Predicate<String> allow, ClassLoader parent) {
		super(urls, parent);
		this.allow = allow;
	}
	
	@Override
	public Class<?> loadClass(String clazz, boolean resolve) throws ClassNotFoundException {
		if(allow.test(clazz)) {
			if(clazz.startsWith("java.")) {
				return super.loadClass(clazz, resolve);
			}
			Class<?> result = findLoadedClass(clazz);
			if(result != null) {
				if(resolve) {
					resolveClass(result);
				}
				return result;
			}
			try(InputStream in = getResourceAsStream(clazz.replace('.', '/') + ".class")) {
				if(in == null) {
					throw new ClassNotFoundException(clazz);
				}
				byte[] rawClass = Util.readStream(in);
				result = defineClass(clazz, rawClass, 0, rawClass.length);
				if(resolve) {
					resolveClass(result);
				}
				return result;
			} catch (IOException e) {
				throw new ClassNotFoundException(clazz, e);
			}
		} else {
			throw new ClassNotFoundException(clazz + " on load block list of bootstrap loader");
		}
	}
}
