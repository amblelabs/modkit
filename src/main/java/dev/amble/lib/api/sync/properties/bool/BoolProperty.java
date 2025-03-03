package dev.amble.lib.api.sync.properties.bool;


import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.properties.Property;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Function;

public class BoolProperty extends Property<Boolean> {

    public static final Type<Boolean> TYPE = new Type<>(Boolean.class, PacketByteBuf::writeBoolean,
            PacketByteBuf::readBoolean);

    public BoolProperty(String name) {
        this(name, false);
    }

    public BoolProperty(String name, Boolean def) {
        this(name, normalize(def));
    }

    public BoolProperty(String name, boolean def) {
        super(TYPE, name, def);
    }

    public BoolProperty(String name, Function<KeyedSyncComponent, Boolean> def) {
        super(TYPE, name, def.andThen(BoolProperty::normalize));
    }

    @Override
    protected BoolValue create(Boolean bool) {
        return new BoolValue(bool);
    }

    @Override
    public BoolValue create(KeyedSyncComponent holder) {
        return (BoolValue) super.create(holder);
    }

    public static boolean normalize(Boolean value) {
        return value != null && value;
    }
}
