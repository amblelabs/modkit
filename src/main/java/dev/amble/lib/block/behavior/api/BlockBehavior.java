package dev.amble.lib.block.behavior.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Experimental
public interface BlockBehavior extends BlockBehaviorLike {

    @Override
    default void unwrap(BlockBehavior[] behaviors) {
        behaviors[idx()] = this;
    }

    default void init(Block block) { }

    default BlockState initDefaultState(Block block, BlockState state) {
        return state;
    }

    default void appendProperties(List<Property<?>> list) { }

    int idx();
}
