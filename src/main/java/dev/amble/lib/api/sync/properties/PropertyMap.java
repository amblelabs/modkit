package dev.amble.lib.api.sync.properties;

import dev.amble.lib.api.sync.Disposable;

import java.util.HashMap;

public class PropertyMap extends HashMap<String, Value<?>> implements Disposable {

    @SuppressWarnings("unchecked")
    public <T> Value<T> getExact(String key) {
        return (Value<T>) this.get(key);
    }

    @Override
    public void dispose() {
        for (Value<?> value : this.values()) {
            value.dispose();
        }

        this.clear();
    }
}
