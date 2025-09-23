package dev.amble.lib.api.sync.manager.client;

import dev.amble.lib.api.sync.Disposable;

public interface ClientRootComponent extends Disposable {
    ClientComponentData data();

    @Override
    default void age() {
        data().age();
    }

    @Override
    default void dispose() {
        data().dispose();
    }

    @Override
    default boolean isAged() {
        return data().isAged();
    }
}
