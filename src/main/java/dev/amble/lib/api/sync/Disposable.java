package dev.amble.lib.api.sync;

public interface Disposable {

    default void age() {
    }

    void dispose();

    default boolean isAged() {
        return false;
    }
}