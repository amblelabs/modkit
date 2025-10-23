package dev.amble.lib.block.behavior.api;

public record Archetype(BlockBehavior... behaviors) implements BlockBehaviorLike {

    @Override
    public void unwrap(BlockBehavior[] behaviors) {
        for (BlockBehavior behavior : this.behaviors) {
            behavior.unwrap(behaviors);
        }
    }
}
