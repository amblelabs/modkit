package dev.amble.lib.block.behavior.base;

import dev.amble.lib.block.ABlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface BlockBehavior<T extends BlockBehavior<T>> extends BlockBehaviorLike {

    @Override
    default boolean isSingle() {
        return true;
    }

    @Override
    default BlockBehavior<?> singleBehavior() {
        return this;
    }

    default void init(Block block) { }

    default BlockState initDefaultState(Block block, BlockState state) {
        return state;
    }

    default void appendProperties(StateManager.Builder<Block, BlockState> builder) { }

    Entry<T> type();

    interface Entry<T extends BlockBehavior<T>> {

        T get(ABlock block);
        void set(ABlock block, BlockBehavior<?> t);

        record Impl<T extends BlockBehavior<T>>(int index) implements Entry<T> {

            public T get(ABlock block) {
                return (T) block.behaviors[index];
            }

            @Override
            public void set(ABlock block, BlockBehavior<?> behavior) {
                block.behaviors[index] = behavior;
            }
        }
    }

}
