package io.github.nuclearfarts.yetanotherloader.impl.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;

import io.github.nuclearfarts.yetanotherloader.api.ApiLoader;
import io.github.nuclearfarts.yetanotherloader.api.plugin.YalPlugin;
import io.github.nuclearfarts.yetanotherloader.api.transformer.TransformerService;
import io.github.nuclearfarts.yetanotherloader.api.transformer.YalTransformer;

public final class MainTransformer implements YalTransformer, YalPlugin {
	private final List<TransformerService> transformers = new ArrayList<>();
	
	@Override
	public boolean transformsClass(String className) {
		for(TransformerService svc : transformers) {
			if(svc.transformsClass(className)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int getWriterFlags(String className) {
		int flags = 0;
		for(TransformerService svc : getTransformers(className)) {
			flags |= svc.getWriterFlags(className);
		}
		return flags;
	}
	
	private List<TransformerService> getTransformers(String className) {
		List<TransformerService> res = new ArrayList<>();
		for(TransformerService svc :  transformers) {
			if(svc.transformsClass(className)) {
				res.add(svc);
			}
		}
		return res;
	}
	
	@Override
	public void transform(String className, ClassNode node) {
		for(TransformerService transformer : getTransformers(className)) {
			transformer.transform(className, node);
		}
	}
	
	@Override
	public void modTransformersLoadedCallback(ClassLoader loader) {
		transformers.addAll(ApiLoader.loadServiceProviders(TransformerService.class, loader));
		Collections.sort(transformers);
	}
}
