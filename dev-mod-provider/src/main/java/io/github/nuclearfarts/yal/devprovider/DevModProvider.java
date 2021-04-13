package io.github.nuclearfarts.yal.devprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import io.github.nuclearfarts.yetanotherloader.api.mod.Mod;
import io.github.nuclearfarts.yetanotherloader.api.mod.ModProvider;

public class DevModProvider implements ModProvider {
	@Override
	public Collection<? extends Mod> getMods() {
		List<Mod> mods = new ArrayList<>();
		try {
			Enumeration<URL> classpathTomlsE = ClassLoader.getSystemResources("mod.toml");
			List<Path> classpathTomls = new ArrayList<>();
			while(classpathTomlsE.hasMoreElements()) {
				URL classpathToml = classpathTomlsE.nextElement();
				String uriParseHack = classpathToml.toURI().toString();
				if(uriParseHack.startsWith("jar:")) {
					String jar = uriParseHack.split("/!")[0];
					URI uri = new URI(jar);
					try {
						FileSystems.getFileSystem(uri);
					} catch(FileSystemNotFoundException e) {
						FileSystems.newFileSystem(uri, Collections.emptyMap(), getClass().getClassLoader());
					}
				}
				classpathTomls.add(Paths.get(classpathToml.toURI()));
			}
			for(Path p : classpathTomls) {
				try(InputStream inStream = Files.newInputStream(p)) {
					mods.add(new DevMod(inStream, p.toAbsolutePath().getParent()));
				}
			}
			return mods;
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
