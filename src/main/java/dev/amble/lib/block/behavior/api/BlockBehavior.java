package dev.amble.lib.block.behavior.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

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

    default void appendProperties(List<Property<?>> list) { }

    int idx();

    interface Entry<T extends BlockBehavior<T>> {

        T get(BlockBehavior<?>[] block);
        void set(BlockBehavior<?>[] block, BlockBehavior<?> t);

        record Impl<T extends BlockBehavior<T>>(int index) implements Entry<T> {

            @Override
            public T get(BlockBehavior<?>[] block) {
                return (T) block[index];
            }

            @Override
            public void set(BlockBehavior<?>[] block, BlockBehavior<?> behavior) {
                block[index] = behavior;
            }
        }
    }

}
