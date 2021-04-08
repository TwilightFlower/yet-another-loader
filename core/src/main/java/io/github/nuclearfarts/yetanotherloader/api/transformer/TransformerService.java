package io.github.nuclearfarts.yetanotherloader.api.transformer;

import org.objectweb.asm.tree.ClassNode;

public interface TransformerService extends Comparable<TransformerService> {
	boolean transformsClass(String className);
	void transform(String className, ClassNode classNode);
	/**
	 * Flags on the ClassWriter this transformer needs.
	 */
	default int getWriterFlags(String className) {
		return 0; 
	}
	
	/**
	 * Higher priority runs first.
	 */
	default int getPriority() {
		return 0;
	}
	
	@Override
	default int compareTo(TransformerService other) {
		return other.getPriority() > getPriority() ? 1 : other.getPriority() == getPriority() ? 0 : -1; // higher value = runs earlier.
	}
}
