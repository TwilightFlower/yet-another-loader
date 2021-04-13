package io.github.nuclearfarts.yetanotherloader.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class NioClassLoader extends ClassLoader {
	private final Collection<Path> roots;
	private final ClassLoader altResourceSource;
	
	public NioClassLoader(Collection<Path> roots, ClassLoader parent) {
		this(roots, parent, null);
	}
	
	public NioClassLoader(Collection<Path> roots, ClassLoader parent, ClassLoader altResourceSource) {
		super(parent);
		this.roots = roots;
		this.altResourceSource = altResourceSource;
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
		if(altResourceSource != null) {
			return altResourceSource.getResource(loc);
		}
		return null;
	}
	
	@Override
	public Enumeration<URL> findResources(String loc) throws IOException {
		Set<URL> resources = new HashSet<>();
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
		if(altResourceSource != null) {
			Enumeration<URL> res = altResourceSource.getResources(loc);
			while(res.hasMoreElements()) {
				resources.add(res.nextElement());
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
		if(altResourceSource != null) {
			return altResourceSource.getResourceAsStream(loc);
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
