package dev.amble.lib.api.sync.properties.integer;

import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;

import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.manager.server.ServerSyncManager;
import dev.amble.lib.api.sync.properties.Property;

public class IntProperty extends Property<Integer> {

    public static final Type<Integer> TYPE = new Type<>(Integer.class, PacketByteBuf::writeInt, PacketByteBuf::readInt);

    public IntProperty(String name, ServerSyncManager manager) {
        this(name, 0, manager);
    }

    public IntProperty(String name, Integer def, ServerSyncManager manager) {
        this(name, normalize(def), manager);
    }

    public IntProperty(String name, int def, ServerSyncManager manager) {
        super(TYPE, name, def, manager);
    }

    public IntProperty(String name, Function<KeyedSyncComponent, Integer> def, ServerSyncManager manager) {
        super(TYPE, name, def.andThen(IntProperty::normalize), manager);
    }

    @Override
    public IntValue create(KeyedSyncComponent holder) {
        return (IntValue) super.create(holder);
    }

    @Override
    protected IntValue create(Integer integer) {
        return new IntValue(integer);
    }

    public static int normalize(Integer value) {
        return value == null ? 0 : value;
    }
}
