package dev.amble.lib.api.sync.properties.bool;

import dev.amble.lib.api.sync.properties.Value;

public class BoolValue extends Value<Boolean> {

    protected BoolValue(Boolean value) {
        super(value);
    }

    @Override
    public void set(Boolean value, boolean sync) {
        super.set(BoolProperty.normalize(value), sync);
    }

    public void toggle() {
        this.flatMap(b -> !b);
    }

    public void toggle(boolean sync) {
        this.flatMap(b -> !b, sync);
    }

    public static Object serializer() {
        return new Serializer<>(BoolProperty.TYPE, BoolValue::new);
    }
}
