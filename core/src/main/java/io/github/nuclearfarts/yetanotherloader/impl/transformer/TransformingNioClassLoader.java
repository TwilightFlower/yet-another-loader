package io.github.nuclearfarts.yetanotherloader.impl.transformer;

import java.nio.file.Path;
import java.util.Collection;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import io.github.nuclearfarts.yetanotherloader.api.transformer.YalTransformer;
import io.github.nuclearfarts.yetanotherloader.impl.util.NioClassLoader;

public class TransformingNioClassLoader extends NioClassLoader {
	private final YalTransformer transformer = YalTransformer.get();
	
	public TransformingNioClassLoader(Collection<Path> roots, ClassLoader parent) {
		super(roots, parent);
	}
	
	public TransformingNioClassLoader(Collection<Path> roots, ClassLoader parent, ClassLoader altResourceSource) {
		super(roots, parent, altResourceSource);
	}
	
	@Override
	protected byte[] transformClass(String name, byte[] clazz) {
		if(transformer.transformsClass(name)) {
			ClassWriter cw = new ClassWriter(transformer.getWriterFlags(name));
			ClassReader reader = new ClassReader(clazz);
			ClassNode node = new ClassNode();
			reader.accept(node, 0);
			transformer.transform(name, node);
			node.accept(cw);
			return cw.toByteArray();
		} else {
			return clazz;
		}
	}
}
