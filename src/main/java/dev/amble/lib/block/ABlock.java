package dev.amble.lib.block;

import dev.amble.lib.block.behavior.api.BlockBehavior;
import dev.amble.lib.block.behavior.api.BlockBehaviorLike;
import dev.amble.lib.block.behavior.api.BlockBehaviors;
import dev.amble.lib.block.behavior.base.*;
import dev.amble.lib.blockentity.ABlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
@SuppressWarnings("deprecation")
public class ABlock extends Block {

    private final RenderBlockBehavior render;
    private final BlockPlacementBehavior placement;
    private final BlockRotationBehavior rotation;
    private final BlockWithEntityBehavior entity;

    private static BlockBehavior[] flatBehavior(BlockBehaviorLike[] groups) {
        BlockBehavior[] behaviors = BlockBehaviors.behaviors.toArray(new BlockBehavior[0]);

        for (BlockBehaviorLike like : groups) {
            like.unwrap(behaviors);
        }

        return behaviors;
    }

    private static ABlockSettings attachProperties(ABlockSettings settings, BlockBehavior[] behaviors) {
        List<Property<?>> properties = new ArrayList<>();

        for (BlockBehavior behavior : behaviors) {
            if (behavior == null) continue;
            behavior.appendProperties(properties);
        }

        return settings.properties(properties.toArray(new Property[0]));
    }

    public ABlock(ABlockSettings settings, BlockBehaviorLike... behaviorGroups) {
        this(settings, flatBehavior(behaviorGroups));
    }

    private ABlock(ABlockSettings settings, BlockBehavior[] behaviors) {
        super(attachProperties(settings, behaviors));

        BlockState defState = this.createDefaultState();

        for (BlockBehavior behavior : behaviors) {
            if (behavior == null) continue;

            behavior.init(this);
            defState = behavior.initDefaultState(this, defState);
        }

        this.setDefaultState(defState);

        this.render = (RenderBlockBehavior) behaviors[BlockBehaviors.RENDER_BLOCK];
        this.placement = (BlockPlacementBehavior) behaviors[BlockBehaviors.BLOCK_PLACEMENT];
        this.rotation = (BlockRotationBehavior) behaviors[BlockBehaviors.BLOCK_ROTATION];
        this.entity = (BlockWithEntityBehavior) behaviors[BlockBehaviors.BLOCK_WITH_ENTITY];
    }

    protected BlockState createDefaultState() {
        return this.getDefaultState();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return render.getRenderType(state);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getPlacementState(this.getDefaultState(), ctx);
    }

    protected @Nullable BlockState getPlacementState(BlockState state, ItemPlacementContext ctx) {
        return this.placement.getPlacementState(state, ctx);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return this.rotation.rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return this.rotation.mirror(state, mirror);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (entity != null && !state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof ABlockEntity blockEntity)
            blockEntity.onBreak(state, world, pos, newState);

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (entity != null && world.getBlockEntity(pos) instanceof ABlockEntity be)
            return be.onUse(state, world, pos, player, hand, hit);

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (entity != null && world.getBlockEntity(pos) instanceof ABlockEntity be)
            be.onPlaced(world, pos, state, placer, stack);

        super.onPlaced(world, pos, state, placer, stack);
    }

    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return entity.createBlockEntity(pos, state);
    }

    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return entity.getTicker(world, state, type);
    }
}
