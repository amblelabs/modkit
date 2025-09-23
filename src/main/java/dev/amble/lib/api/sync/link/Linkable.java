package dev.amble.lib.api.sync.link;

import dev.amble.lib.api.sync.RootComponent;

import java.util.UUID;

public interface Linkable<R extends RootComponent> {
	void link(R tardis);

	void link(UUID id);

	RootRef<R> parent();

	default boolean isLinked() {
		return this.parent() != null && this.parent().isPresent();
	}

	/**
	 * @implNote This method is called when the RootRef instance gets created. This
	 *           means that the ref is no longer null BUT the root instance still
	 *           could be missing.
	 */
	default void onLinked() {
	}
}
