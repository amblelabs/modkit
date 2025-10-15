package dev.amble.lib.block.behavior.base;

public interface BlockBehaviorLike {
    default BlockBehaviorLike[] allBehaviors() {
        return null;
    }

    default BlockBehavior<?> singleBehavior() {
        return null;
    }

    boolean isSingle();
}
