package dev.amble.lib.block.behavior.base;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;

public class RenderBlockBehavior implements BlockBehavior<RenderBlockBehavior> {

    public static final Entry<RenderBlockBehavior> IDX = BlockBehaviors.register(new RenderBlockBehavior());

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public Entry<RenderBlockBehavior> type() {
        return IDX;
    }
}
