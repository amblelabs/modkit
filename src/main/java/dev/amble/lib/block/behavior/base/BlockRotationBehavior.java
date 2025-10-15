package dev.amble.lib.block.behavior.base;

import net.minecraft.block.BlockState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;

public class BlockRotationBehavior implements BlockBehavior<BlockRotationBehavior> {

    public static final Entry<BlockRotationBehavior> IDX = BlockBehaviors.register(new BlockRotationBehavior());

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state;
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state;
    }

    @Override
    public Entry<BlockRotationBehavior> type() {
        return IDX;
    }
}
