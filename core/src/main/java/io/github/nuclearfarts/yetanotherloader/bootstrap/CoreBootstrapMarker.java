package io.github.nuclearfarts.yetanotherloader.bootstrap;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.util.ASMifier;
import org.tomlj.TomlTable;

public class CoreBootstrapMarker implements IncludeInBootstrapMarker {
	@Override
	public Class<?>[] getAdditionalClasses() {
		return new Class[] {
				ClassVisitor.class,
				Analyzer.class,
				Remapper.class,
				ClassNode.class,
				ASMifier.class,
				Level.class,
				TomlTable.class
		};
	}
	
	@Override
	public String[] getAdditionalNames() {
		return new String[] {
			"javax.annotation.CheckForNull",
			"org.antlr.v4.runtime.Lexer",
			"org.apache.logging.log4j.core.Logger"
		};
	}
}
