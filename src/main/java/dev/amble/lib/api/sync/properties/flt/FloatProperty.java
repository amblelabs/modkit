package dev.amble.lib.api.sync.properties.flt;


import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;

import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.manager.server.ServerSyncManager;
import dev.amble.lib.api.sync.properties.Property;

public class FloatProperty extends Property<Float> {

    public static final Type<Float> TYPE = new Type<>(Float.class, PacketByteBuf::writeFloat,
            PacketByteBuf::readFloat);

    public FloatProperty(String name, ServerSyncManager manager) {
        this(name, 0, manager);
    }

    public FloatProperty(String name, Float def, ServerSyncManager manager) {
        this(name, normalize(def), manager);
    }

    public FloatProperty(String name, float def, ServerSyncManager manager) {
        super(TYPE, name, def, manager);
    }

    public FloatProperty(String name, Function<KeyedSyncComponent, Float> def, ServerSyncManager manager) {
        super(TYPE, name, def.andThen(FloatProperty::normalize), manager);
    }

    @Override
    public FloatValue create(KeyedSyncComponent holder) {
        return (FloatValue) super.create(holder);
    }

    @Override
    protected FloatValue create(Float flt) {
        return new FloatValue(flt);
    }

    public static float normalize(Float value) {
        return value == null ? 0 : value;
    }
}
