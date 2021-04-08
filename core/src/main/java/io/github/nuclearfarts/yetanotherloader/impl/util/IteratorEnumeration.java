package io.github.nuclearfarts.yetanotherloader.impl.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumeration<T> implements Enumeration<T> {
	private final Iterator<T> iter;
	
	public IteratorEnumeration(Iterator<T> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasMoreElements() {
		return iter.hasNext();
	}

	@Override
	public T nextElement() {
		return iter.next();
	}

}
