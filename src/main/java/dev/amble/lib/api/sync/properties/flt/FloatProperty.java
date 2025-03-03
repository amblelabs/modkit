package dev.amble.lib.api.sync.properties.flt;


import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.properties.Property;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Function;

public class FloatProperty extends Property<Float> {

    public static final Type<Float> TYPE = new Type<>(Float.class, PacketByteBuf::writeFloat,
            PacketByteBuf::readFloat);

    public FloatProperty(String name) {
        this(name, 0);
    }

    public FloatProperty(String name, Float def) {
        this(name, normalize(def));
    }

    public FloatProperty(String name, float def) {
        super(TYPE, name, def);
    }

    public FloatProperty(String name, Function<KeyedSyncComponent, Float> def) {
        super(TYPE, name, def.andThen(FloatProperty::normalize));
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
