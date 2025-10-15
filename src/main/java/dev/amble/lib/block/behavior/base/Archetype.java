package dev.amble.lib.block.behavior.base;

public record Archetype(BlockBehavior<?>... behaviors) implements BlockBehaviorLike {
    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public BlockBehavior<?>[] allBehaviors() {
        return behaviors;
    }
}
