package dev.amble.lib.block.behavior.api;

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
