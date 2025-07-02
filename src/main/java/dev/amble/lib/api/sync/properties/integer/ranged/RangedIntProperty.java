package dev.amble.lib.api.sync.properties.integer.ranged;


import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;

import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.manager.server.ServerSyncManager;
import dev.amble.lib.api.sync.properties.Property;

public class RangedIntProperty extends Property<Integer> {

    public static final Type<Integer> TYPE = new Type<>(Integer.class, PacketByteBuf::writeInt, PacketByteBuf::readInt);

    private final int min;
    private final int max;

    public RangedIntProperty(String name, int max, ServerSyncManager manager) {
        this(name, 0, max, 0, manager);
    }

    public RangedIntProperty(String name, int max, Integer def, ServerSyncManager manager) {
        this(name, 0, max, normalize(0, max, def), manager);
    }

    public RangedIntProperty(String name, int min, int max, Integer def, ServerSyncManager manager) {
        super(TYPE, name, normalize(min, max, def), manager);

        this.min = min;
        this.max = max;
    }

    public RangedIntProperty(String name, int max, int def, ServerSyncManager manager) {
        super(TYPE, name, def, manager);

        this.min = 0;
        this.max = max;
    }

    public RangedIntProperty(String name, int min, int max, Function<KeyedSyncComponent, Integer> def, ServerSyncManager manager) {
        super(TYPE, name, def.andThen(i -> RangedIntProperty.normalize(min, max, i)), manager);

        this.min = min;
        this.max = max;
    }

    @Override
    public RangedIntValue create(KeyedSyncComponent holder) {
        return (RangedIntValue) super.create(holder);
    }

    @Override
    protected RangedIntValue create(Integer integer) {
        return new RangedIntValue(integer);
    }

    public static int normalize(int min, int max, Integer value) {
        return MathHelper.clamp(value == null ? 0 : value, min, max);
    }

    public static int normalize(RangedIntProperty property, Integer value) {
        return normalize(property.min, property.max, value);
    }
}
