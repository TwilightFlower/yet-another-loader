package io.github.nuclearfarts.yetanotherloader.api;

import java.nio.file.Path;
import java.util.Collection;

public interface GameProvider {
	/**
	 * 
	 * @return The game provider.
	 * @throws IllegalStateException
	 */
	static GameProvider get() throws IllegalStateException {
		GameProvider result = ApiLoader.getLoadedSingle(GameProvider.class);
		if(result == null) {
			throw new IllegalStateException("Attempt to access game provider before it's loaded");
		} else {
			return result;
		}
	}
	
	/**
	 * Gets the roots of all directory structures considered part of the game. This includes libraries.
	 */
	Collection<Path> getFsRoots();
	
	/**
	 * Launches the game.
	 */
	void launch(ClassLoader loader, String[] gameArgs);
}
