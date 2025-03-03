package dev.amble.lib.api.sync.manager;

import java.util.HashMap;
import java.util.UUID;

import dev.amble.lib.api.sync.RootComponent;

public class RootMap<T extends RootComponent> extends HashMap<UUID, T> {

    public T put(T t) {
        return this.put(t.getUuid(), t);
    }
}