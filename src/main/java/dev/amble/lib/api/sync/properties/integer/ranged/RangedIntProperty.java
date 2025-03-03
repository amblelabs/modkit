package dev.amble.lib.api.sync.properties.integer.ranged;


import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.properties.Property;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public class RangedIntProperty extends Property<Integer> {

    public static final Type<Integer> TYPE = new Type<>(Integer.class, PacketByteBuf::writeInt, PacketByteBuf::readInt);

    private final int min;
    private final int max;

    public RangedIntProperty(String name, int max) {
        this(name, 0, max, 0);
    }

    public RangedIntProperty(String name, int max, Integer def) {
        this(name, 0, max, normalize(0, max, def));
    }

    public RangedIntProperty(String name, int min, int max, Integer def) {
        super(TYPE, name, normalize(min, max, def));

        this.min = min;
        this.max = max;
    }

    public RangedIntProperty(String name, int max, int def) {
        super(TYPE, name, def);

        this.min = 0;
        this.max = max;
    }

    public RangedIntProperty(String name, int min, int max, Function<KeyedSyncComponent, Integer> def) {
        super(TYPE, name, def.andThen(i -> RangedIntProperty.normalize(min, max, i)));

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
