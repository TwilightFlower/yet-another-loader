package io.github.nuclearfarts.yetanotherloader.api.transformer;

import org.objectweb.asm.tree.ClassNode;

import io.github.nuclearfarts.yetanotherloader.api.ApiLoader;

/**
 * API for manually invoking the YAL transformer chain on a class. Can be useful for classloader hijinks.
 */
public interface YalTransformer {
	/**
	 * Gets the transformer.
	 * @throws IllegalStateException if the transformer hasn't been initialized yet
	 */
	static YalTransformer get() throws IllegalStateException {
		YalTransformer result = ApiLoader.getLoadedSingle(YalTransformer.class);
		if(result == null) {
			throw new IllegalStateException("Attempted to access YALTransformer before it was loaded");
		} else {
			return result;
		}
	}
	
	/**
	 * This one's pretty self explanatory.
	 */
	boolean transformsClass(String className);
	
	/**
	 * Creates a transforming class visitor for the specific class given, outputting to the given class visitor.
	 * Any calls on this visitor (or any visitors of its elements) after visitEnd may be ignored.
	 */
	void transform(String className, ClassNode node);
	
	/**
	 * 
	 */
	int getWriterFlags(String className);
}
