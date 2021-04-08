package io.github.nuclearfarts.yal.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.util.ReEntranceLock;

import io.github.nuclearfarts.yal.mixin.api.SideNameProvider;
import io.github.nuclearfarts.yetanotherloader.api.ApiLoader;
import io.github.nuclearfarts.yetanotherloader.api.YalApi;
import io.github.nuclearfarts.yetanotherloader.api.mod.Mod;
import io.github.nuclearfarts.yetanotherloader.api.ApiLoader.ApiProviderNotFoundException;
import io.github.nuclearfarts.yetanotherloader.api.ApiLoader.MultipleApiProvidersException;
import io.github.nuclearfarts.yetanotherloader.api.plugin.YalPlugin;
import io.github.nuclearfarts.yetanotherloader.api.transformer.TransformerService;
import io.github.nuclearfarts.yetanotherloader.api.transformer.YalTransformer;
import io.github.nuclearfarts.yetanotherloader.impl.util.Util;

public class YalMixinService implements IMixinService, YalPlugin, IClassProvider, IClassBytecodeProvider, TransformerService {
	private final ReEntranceLock lock = new ReEntranceLock(2);
	private static ClassLoader gameLoader;
	private static String sideName;
	private static IMixinTransformerFactory transformerFactory;
	private static IMixinTransformer mixinTransformer;
	private static ThreadLocal<Boolean> shouldApplyMixins = new ThreadLocal<Boolean>();
	
	static {
		shouldApplyMixins.set(true);
	}

	@Override
	public String getName() {
		return "YalMixinService";
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void prepare() { }

	@Override
	public Phase getInitialPhase() {
		return Phase.DEFAULT;
	}

	@Override
	public void init() { }

	@Override
	public void beginPhase() { }

	@Override
	public void checkEnv(Object bootSource) { }

	@Override
	public ReEntranceLock getReEntranceLock() {
		return lock;
	}

	@Override
	public IClassProvider getClassProvider() {
		return this;
	}

	@Override
	public IClassBytecodeProvider getBytecodeProvider() {
		return this;
	}

	@Override
	public ITransformerProvider getTransformerProvider() {
		return null;
	}

	@Override
	public IClassTracker getClassTracker() {
		return null;
	}

	@Override
	public IMixinAuditTrail getAuditTrail() {
		return null;
	}

	@Override
	public Collection<String> getPlatformAgents() {
		return Collections.singletonList("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
	}

	@Override
	public IContainerHandle getPrimaryContainer() {
		try {
			return new ContainerHandleURI(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<IContainerHandle> getMixinContainers() {
		return Collections.emptyList();
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return gameLoader.getResourceAsStream(name);
	}

	@Override
	public String getSideName() {
		return sideName;
	}

	@Override
	public CompatibilityLevel getMinCompatibilityLevel() {
		return null;
	}

	@Override
	public CompatibilityLevel getMaxCompatibilityLevel() {
		return null;
	}
	
	@Deprecated
	@Override
	public URL[] getClassPath() {
		return new URL[0];
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		return gameLoader.loadClass(name);
	}
	
	@Override
	public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
		return Class.forName(name, initialize, gameLoader);
	}
	
	@Override
	public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
		return Class.forName(name, initialize, getClass().getClassLoader());
	}
	
	@Override
	public void gameLoadedCallback(ClassLoader loader) {
		gameLoader = loader;
		MixinBootstrap.init();
		MixinEnvironment.init(Phase.DEFAULT);
		for(Mod m : YalApi.INSTANCE.getLoadedMods()) {
			Object mixins = m.getConfigMap().get("mixins");
			if(mixins instanceof List) {
				List<?> mixinList = (List<?>) mixins;
				for(Object o : mixinList) {
					if(o instanceof String) {
						Mixins.addConfiguration((String) o);
					}
				}
			}
		}
		mixinTransformer = transformerFactory.createTransformer();
	}
	
	@Override
	public void pluginLoadCallback() {
		try {
			sideName = ApiLoader.loadSingle(SideNameProvider.class, DefaultSideNameProvider.class, getClass().getClassLoader()).getSideName();
		} catch (ApiProviderNotFoundException e) {
			System.out.println("Side name provider could not be found. This shouldn't happen, but is not fatal.");
			sideName = "universal";
		} catch (MultipleApiProvidersException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
		return getClassNode(name, true);
	}

	@Override
	public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
		String resLoc = name.replace('.', '/') + ".class";
		try(InputStream inStream = gameLoader.getResourceAsStream(resLoc)) {
			if(inStream != null) {
				byte[] classBytes = Util.readStream(inStream);
				ClassReader reader = new ClassReader(classBytes);
				ClassNode node = new ClassNode();
				reader.accept(node, 0);
				if(runTransformers && YalApi.INSTANCE.isOnTransformationPath(resLoc)) {
					shouldApplyMixins.set(false);
					YalTransformer.get().transform(name, node);
					shouldApplyMixins.set(true);
				}
				return node;
			} else {
				throw new ClassNotFoundException("no resource for " + name);
			}
		}
	}
	
	@Override
	public int getPriority() {
		return Integer.MIN_VALUE;
	}
	
	@Override
	public boolean transformsClass(String name) {
		return shouldApplyMixins.get(); // TODO actually compute this.
	}
	
	@Override
	public void transform(String name, ClassNode node) {
		mixinTransformer.transformClass(MixinEnvironment.getCurrentEnvironment(), name, node);
	}

	@Override
	public void offer(IMixinInternal internal) {
		if(internal instanceof IMixinTransformerFactory) {
			transformerFactory = (IMixinTransformerFactory) internal;
		}
	}
	
}
