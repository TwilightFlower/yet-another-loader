package io.github.nuclearfarts.yetanotherloader.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public class NioClassLoader extends ClassLoader {
	private final Collection<Path> roots;
	
	public NioClassLoader(Collection<Path> roots, ClassLoader parent) {
		super(parent);
		this.roots = roots;
	}
	
	@Override
	public URL findResource(String loc) {
		for(Path root : roots) {
			Path res = root.resolve(loc);
			if(Files.exists(res) && Files.isRegularFile(res)) {
				try {
					return res.toUri().toURL();
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
	
	@Override
	public Enumeration<URL> findResources(String loc) {
		List<URL> resources = new ArrayList<>();
		for(Path root : roots) {
			Path res = root.resolve(loc);
			if(Files.exists(res) && Files.isRegularFile(res)) {
				try {
					resources.add(res.toUri().toURL());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return new IteratorEnumeration<>(resources.iterator());
	}
	
	private InputStream findResourceAsStream(String loc) throws IOException {
		for(Path root : roots) {
			Path res = root.resolve(loc);
			if(Files.exists(res) && Files.isRegularFile(res)) {
				return Files.newInputStream(res);
			}
		}
		return null;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String resLoc = name.replace('.', '/') + ".class";
		try(InputStream inStream = findResourceAsStream(resLoc)) {
			if(inStream != null) {
				byte[] rawClass = Util.readStream(inStream);
				rawClass = transformClass(name, rawClass);
				return defineClass(name, rawClass, 0, rawClass.length);
			} else {
				throw new ClassNotFoundException(name + ": no resource found");
			}
		} catch (IOException e) {
			throw new ClassNotFoundException(name, e);
		}
	}
	
	protected byte[] transformClass(String name, byte[] clazz) {
		return clazz;
	}
}
