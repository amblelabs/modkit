package dev.amble.lib.block.behavior;

import dev.amble.lib.block.behavior.base.RenderBlockBehavior;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;

public class InvisibleBlockBehavior extends RenderBlockBehavior {

    public static InvisibleBlockBehavior behavior = new InvisibleBlockBehavior();

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
}
