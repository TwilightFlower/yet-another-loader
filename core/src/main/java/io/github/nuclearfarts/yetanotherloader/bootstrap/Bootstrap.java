package io.github.nuclearfarts.yetanotherloader.bootstrap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class Bootstrap {
	public static void main(String[] args) throws IOException {
		List<URL> urls = new ArrayList<>();
		if(System.getProperty("yal.loadlibs") != null) {
			for(Path p : Files.newDirectoryStream(Paths.get("yallibs"))) {
				urls.add(p.toUri().toURL());
			}
		}
		ClassLoader bootstrapParent;
		if(System.getProperty("yal.bootstrap.reload") != null) {
			bootstrapParent = null;
			for(IncludeInBootstrapMarker marker : ServiceLoader.load(IncludeInBootstrapMarker.class, Bootstrap.class.getClassLoader())) {
				urls.add(marker.getClass().getProtectionDomain().getCodeSource().getLocation());
				for(Class<?> c : marker.getAdditionalClasses()) {
					urls.add(c.getProtectionDomain().getCodeSource().getLocation());
				}
				for(String s : marker.getAdditionalNames()) {
					Class<?> c;
					try {
						c = Class.forName(s);
						urls.add(c.getProtectionDomain().getCodeSource().getLocation());
					} catch (ClassNotFoundException e) { }
				}
			}
		} else {
			bootstrapParent = Bootstrap.class.getClassLoader();
		}
		ClassLoader bootstrapClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), bootstrapParent);
		try {
			Class<?> yalLoader = bootstrapClassLoader.loadClass("io.github.nuclearfarts.yetanotherloader.impl.YalMainLoader");
			Method m = yalLoader.getMethod("loadProcess", Path.class, String[].class, ClassLoader.class);
			m.invoke(null, Paths.get("yalplugins"), args, Bootstrap.class.getClassLoader());
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
	}
}
