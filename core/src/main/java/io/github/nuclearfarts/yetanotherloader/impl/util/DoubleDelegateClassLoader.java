package io.github.nuclearfarts.yetanotherloader.impl.util;

public class DoubleDelegateClassLoader extends ClassLoader {
	private final ClassLoader delegate;
	public DoubleDelegateClassLoader(ClassLoader first, ClassLoader second) {
		super(first);
		delegate = second;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return delegate.loadClass(name);
	}
}
