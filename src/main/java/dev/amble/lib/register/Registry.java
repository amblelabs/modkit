package dev.amble.lib.register;

public interface Registry {
    default void onCommonInit() {
    }

    default void onClientInit() {
    }

    default void onServerInit() {
    }
}
