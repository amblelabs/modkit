package dev.amble.lib.block;

import dev.amble.lib.block.behavior.*;
import dev.amble.lib.block.behavior.base.*;
import dev.amble.lib.blockentity.ABlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
@SuppressWarnings("deprecation")
public class ABlock extends Block {

    public final BlockBehavior<?>[] behaviors;

    public ABlock(Settings settings, BlockBehaviorLike... behaviors) {
        super(settings);

        this.behaviors = BlockBehaviors.behaviors.toArray(new BlockBehavior[0]);

        for (BlockBehaviorLike like : behaviors) {
            this.unwrap(like);
        }

        BlockState defState = this.createDefaultState();

        for (BlockBehavior<?> behavior : this.behaviors) {
            behavior.init(this);
            defState = behavior.initDefaultState(this, defState);
        }

        this.setDefaultState(defState);
    }

    private void unwrap(BlockBehaviorLike like) {
        if (like.isSingle()) {
            BlockBehavior<?> behavior = like.singleBehavior();
            behavior.type().set(this, behavior);
            return;
        }

        for (BlockBehaviorLike behavior : like.allBehaviors()) {
            unwrap(behavior);
        }
    }

    protected BlockState createDefaultState() {
        return this.getDefaultState();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return RenderBlockBehavior.IDX.get(this).getRenderType(state);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getPlacementState(this.getDefaultState(), ctx);
    }

    protected @Nullable BlockState getPlacementState(BlockState state, ItemPlacementContext ctx) {
        return BlockPlacementBehavior.IDX.get(this).getPlacementState(state, ctx);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return BlockRotationBehavior.IDX.get(this).rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return BlockRotationBehavior.IDX.get(this).mirror(state, mirror);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        for (BlockBehavior<?> behavior : behaviors) {
            behavior.appendProperties(builder);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (BlockWithEntityBehavior.IDX.get(this) != null && !state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof ABlockEntity blockEntity)
            blockEntity.onBreak(state, world, pos, newState);

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (BlockWithEntityBehavior.IDX.get(this) != null && world.getBlockEntity(pos) instanceof ABlockEntity be)
            be.onPlaced(world, pos, state, placer, stack);

        super.onPlaced(world, pos, state, placer, stack);
    }

    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockWithEntityBehavior.IDX.get(this).createBlockEntity(pos, state);
    }

    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockWithEntityBehavior.IDX.get(this).getTicker(world, state, type);
    }
}
