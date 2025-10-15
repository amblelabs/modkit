package dev.amble.lib.block.behavior.base;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;

public class BlockPlacementBehavior implements BlockBehavior<BlockPlacementBehavior> {

    public static final Entry<BlockPlacementBehavior> IDX = BlockBehaviors.register(new BlockPlacementBehavior());

    public BlockState getPlacementState(BlockState state, ItemPlacementContext ctx) {
        return state;
    }

    @Override
    public Entry<BlockPlacementBehavior> type() {
        return IDX;
    }
}
