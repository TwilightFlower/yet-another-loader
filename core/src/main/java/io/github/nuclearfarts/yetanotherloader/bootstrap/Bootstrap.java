package io.github.nuclearfarts.yetanotherloader.bootstrap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Bootstrap {
	public static void main(String[] args) throws IOException {
		
		String[] allowLoad = {"io.github.nuclearfarts.yetanotherloader", "org.ow2.asm", "org.tomlj"};
		Predicate<String> allow;
		if(System.getProperty("yal.loaderBlocklist") != null) {
			 allow = s -> {
				for(String str : allowLoad) {
					if(s.startsWith(str)) {
						return true;
					}
				}
				return false;
			};
		} else {
			allow = s -> true;
		}
		
		
		List<URL> urls = new ArrayList<>();
		if(System.getProperty("yal.loadlibs") != null) {
			for(Path p : Files.newDirectoryStream(Paths.get("yallibs"))) {
				urls.add(p.toUri().toURL());
			}
		}
		ClassLoader bootstrapClassLoader = Bootstrap.class.getClassLoader(); //TODO figure out why this breaks reflection new BootstrapClassLoader(urls.toArray(new URL[urls.size()]), allow, Bootstrap.class.getClassLoader());
		try {
			Class<?> yalLoader = bootstrapClassLoader.loadClass("io.github.nuclearfarts.yetanotherloader.impl.YalMainLoader");
			Method m = yalLoader.getMethod("loadProcess", Path.class, String[].class);
			m.invoke(null, Paths.get("yalplugins"), args);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
	}
}
