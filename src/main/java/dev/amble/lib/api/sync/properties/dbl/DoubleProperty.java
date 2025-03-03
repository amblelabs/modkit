package dev.amble.lib.api.sync.properties.dbl;


import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;

import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.manager.server.ServerSyncManager;
import dev.amble.lib.api.sync.properties.Property;

public class DoubleProperty extends Property<Double> {

    public static final Type<Double> TYPE = new Type<>(Double.class, PacketByteBuf::writeDouble,
            PacketByteBuf::readDouble);

    public DoubleProperty(String name, ServerSyncManager manager) {
        this(name, 0, manager);
    }

    public DoubleProperty(String name, Double def, ServerSyncManager manager) {
        this(name, normalize(def), manager);
    }

    public DoubleProperty(String name, double def, ServerSyncManager manager) {
        super(TYPE, name, def, manager);
    }

    public DoubleProperty(String name, Function<KeyedSyncComponent, Double> def, ServerSyncManager manager) {
        super(TYPE, name, def.andThen(DoubleProperty::normalize), manager);
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
