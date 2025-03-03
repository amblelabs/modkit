package dev.amble.lib.api.sync.properties.dbl;

import dev.amble.lib.api.sync.properties.Value;

public class DoubleValue extends Value<Double> {

    protected DoubleValue(Double value) {
        super(value);
    }

    @Override
    public void set(Double value, boolean sync) {
        super.set(DoubleProperty.normalize(value), sync);
    }

    public static Object serializer() {
        return new Serializer<>(DoubleProperty.TYPE, DoubleValue::new);
    }
}
