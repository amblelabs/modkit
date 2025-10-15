package dev.amble.lib.block.behavior.api;

import dev.amble.lib.block.behavior.base.BlockPlacementBehavior;
import dev.amble.lib.block.behavior.base.BlockRotationBehavior;
import dev.amble.lib.block.behavior.base.RenderBlockBehavior;

import java.util.ArrayList;
import java.util.List;

public class BlockBehaviors {

    public static final List<BlockBehavior> behaviors = new ArrayList<>();

    public static final int BLOCK_PLACEMENT = BlockBehaviors.register(new BlockPlacementBehavior());
    public static final int BLOCK_ROTATION = BlockBehaviors.register(new BlockRotationBehavior());
    public static final int BLOCK_WITH_ENTITY = BlockBehaviors.register(null);
    public static final int RENDER_BLOCK = BlockBehaviors.register(new RenderBlockBehavior());

    public static <T extends BlockBehavior> int register(T t) {
        behaviors.add(t);
        return behaviors.size() - 1;
    }
}
