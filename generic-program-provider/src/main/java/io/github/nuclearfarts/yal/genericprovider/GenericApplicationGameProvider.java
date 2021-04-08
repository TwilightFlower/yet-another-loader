package io.github.nuclearfarts.yal.genericprovider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;

import io.github.nuclearfarts.yetanotherloader.api.GameProvider;

public class GenericApplicationGameProvider implements GameProvider {
	private final Path target;
	private Path fsRoot;
	private Manifest manifest;
	private String mainClass;
	public GenericApplicationGameProvider() {
		target = Paths.get(System.getProperty("yal.generic.target", "target.jar"));
	}
	
	@Override
	public Collection<Path> getFsRoots() {
		try {
			FileSystem zipfs = FileSystems.newFileSystem(target, getClass().getClassLoader());
			fsRoot = zipfs.getPath("/");
			try(InputStream inStream = Files.newInputStream(fsRoot.resolve("META-INF").resolve("MANIFEST.MF"))) {
				manifest = new Manifest(inStream);
			}
			mainClass = manifest.getMainAttributes().getValue("Main-Class");
			List<Path> p = new ArrayList<>();
			p.add(fsRoot);
			String cp = manifest.getMainAttributes().getValue("Class-Path");
			if(cp != null) {
				for(String cpe : cp.split(" ")) {
					p.add(target.toAbsolutePath().getParent().resolve(cpe));
				}
			}
			return p;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void launch(ClassLoader loader, String[] args) {
		try {
			Class<?> main = loader.loadClass(mainClass);
			Method m = main.getMethod("main", String[].class);
			m.invoke(null, (Object) args);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
