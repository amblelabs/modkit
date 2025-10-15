package dev.amble.lib.block.behavior.base;

import java.util.ArrayList;
import java.util.List;

public class BlockBehaviors {

    public static final List<BlockBehavior<?>> behaviors = new ArrayList<>();

    public static <T extends BlockBehavior<T>> BlockBehavior.Entry<T> register(T t) {
        BlockBehaviors.behaviors.add(t);
        return new BlockBehavior.Entry.Impl<>(behaviors.size() - 1);
    }
}
