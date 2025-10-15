package dev.amble.lib.block.behavior.base;

import dev.amble.lib.block.behavior.api.BlockBehavior;
import dev.amble.lib.block.behavior.api.BlockBehaviors;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;

public class BlockPlacementBehavior implements BlockBehavior<BlockPlacementBehavior> {

    public BlockState getPlacementState(BlockState state, ItemPlacementContext ctx) {
        return state;
    }

    @Override
    public int idx() {
        return BlockBehaviors.BLOCK_PLACEMENT;
    }
}
