package dev.amble.lib.block.behavior.base;

import dev.amble.lib.block.behavior.api.BlockBehavior;
import dev.amble.lib.block.behavior.api.BlockBehaviors;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;

public class RenderBlockBehavior implements BlockBehavior {

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public int idx() {
        return BlockBehaviors.RENDER_BLOCK;
    }
}
