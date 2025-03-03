package dev.amble.lib.api.sync.properties.dbl;


import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.properties.Property;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Function;

public class DoubleProperty extends Property<Double> {

    public static final Type<Double> TYPE = new Type<>(Double.class, PacketByteBuf::writeDouble,
            PacketByteBuf::readDouble);

    public DoubleProperty(String name) {
        this(name, 0);
    }

    public DoubleProperty(String name, Double def) {
        this(name, normalize(def));
    }

    public DoubleProperty(String name, double def) {
        super(TYPE, name, def);
    }

    public DoubleProperty(String name, Function<KeyedSyncComponent, Double> def) {
        super(TYPE, name, def.andThen(DoubleProperty::normalize));
    }

    @Override
    public DoubleValue create(KeyedSyncComponent holder) {
        return (DoubleValue) super.create(holder);
    }

    @Override
    protected DoubleValue create(Double integer) {
        return new DoubleValue(integer);
    }

    public static double normalize(Double value) {
        return value == null ? 0 : value;
    }
}
