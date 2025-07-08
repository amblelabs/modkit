package dev.amble.lib.api.sync.properties.flt;

import dev.amble.lib.api.sync.properties.Value;

public class FloatValue extends Value<Float> {

    protected FloatValue(Float value) {
        super(value);
    }

    @Override
    public void set(Float value, boolean sync) {
        super.set(FloatProperty.normalize(value), sync);
    }

    public static Object serializer() {
        return new Serializer<>(FloatProperty.TYPE, FloatValue::new);
    }
}
