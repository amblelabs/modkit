package dev.amble.lib.block.behavior.horizontal;

import dev.amble.lib.block.behavior.base.BlockPlacementBehavior;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.Direction;

public class HorizontalBlockPlacementBehavior extends BlockPlacementBehavior {

    public static final HorizontalBlockPlacementBehavior behaviorFacePlayer = new HorizontalBlockPlacementBehavior(true);
    public static final HorizontalBlockPlacementBehavior behavior = new HorizontalBlockPlacementBehavior(false);

    public static HorizontalBlockPlacementBehavior get(boolean facePlayer) {
        return facePlayer ? behaviorFacePlayer : behavior;
    }

    private final boolean facePlayer;

    public HorizontalBlockPlacementBehavior(boolean facePlayer) {
        this.facePlayer = facePlayer;
    }

    @Override
    public BlockState getPlacementState(BlockState state, ItemPlacementContext ctx) {
        Direction direction = ctx.getHorizontalPlayerFacing();
        return state.with(HorizontalBlockBehavior.FACING, facePlayer ? direction.getOpposite() : direction);
    }
}
