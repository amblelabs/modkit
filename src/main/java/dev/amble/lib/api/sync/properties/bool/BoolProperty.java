package dev.amble.lib.api.sync.properties.bool;


import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;

import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.manager.server.ServerSyncManager;
import dev.amble.lib.api.sync.properties.Property;

public class BoolProperty extends Property<Boolean> {

    public static final Type<Boolean> TYPE = new Type<>(Boolean.class, PacketByteBuf::writeBoolean,
            PacketByteBuf::readBoolean);

    public BoolProperty(String name, ServerSyncManager manager) {
        this(name, false, manager);
    }

    public BoolProperty(String name, Boolean def, ServerSyncManager manager) {
        this(name, normalize(def), manager);
    }

    public BoolProperty(String name, boolean def, ServerSyncManager manager) {
        super(TYPE, name, def, manager  );
    }

    public BoolProperty(String name, Function<KeyedSyncComponent, Boolean> def, ServerSyncManager manager) {
        super(TYPE, name, def.andThen(BoolProperty::normalize), manager);
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
