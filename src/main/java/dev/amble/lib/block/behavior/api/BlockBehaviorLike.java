package dev.amble.lib.block.behavior.api;

public interface BlockBehaviorLike {
    default BlockBehaviorLike[] allBehaviors() {
        return null;
    }

    default BlockBehavior<?> singleBehavior() {
        return null;
    }

    boolean isSingle();
}
