package dev.amble.lib.block.behavior.horizontal;

import dev.amble.lib.block.behavior.api.Archetype;
import dev.amble.lib.block.behavior.base.BlockRotationBehavior;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

import java.util.List;

public class HorizontalBlockBehavior extends BlockRotationBehavior {

    public static final HorizontalBlockBehavior behavior = new HorizontalBlockBehavior();
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    @Override
    public BlockState initDefaultState(Block block, BlockState state) {
        return state.with(FACING, Direction.NORTH);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public void appendProperties(List<Property<?>> list) {
        list.add(FACING);
    }

    public static Direction getFacing(BlockState state) {
        return state.get(FACING);
    }

    private static final Archetype archFacePlayer = new Archetype(behavior, HorizontalBlockPlacementBehavior.behaviorFacePlayer);
    private static final Archetype arch = new Archetype(behavior, HorizontalBlockPlacementBehavior.behavior);

    public static Archetype withPlacement(boolean facePlayer) {
        return facePlayer ? archFacePlayer : arch;
    }
}
