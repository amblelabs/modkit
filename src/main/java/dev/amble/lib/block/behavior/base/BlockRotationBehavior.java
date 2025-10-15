package dev.amble.lib.block.behavior.base;

import dev.amble.lib.block.behavior.api.BlockBehavior;
import dev.amble.lib.block.behavior.api.BlockBehaviors;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;

public class BlockRotationBehavior implements BlockBehavior {

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state;
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state;
    }

    @Override
    public int idx() {
        return BlockBehaviors.BLOCK_ROTATION;
    }
}
