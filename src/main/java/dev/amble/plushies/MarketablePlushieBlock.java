package dev.amble.plushies;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.block.ABlock;
import dev.amble.lib.block.ABlockSettings;
import dev.amble.lib.block.behavior.base.BlockWithEntityBehavior;
import dev.amble.lib.client.bedrock.BedrockEntityModel;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MarketablePlushieBlock extends ABlock implements BlockEntityProvider {

    public static final int MAX_ROTATION_INDEX = RotationPropertyHelper.getMax();
    private static final int MAX_ROTATIONS = MAX_ROTATION_INDEX + 1;
    public static final IntProperty ROTATION = Properties.ROTATION;
    public static final BooleanProperty STACKED = BooleanProperty.of("stacked");
    protected static final VoxelShape SHAPE = Block.createCuboidShape(4.0F, 0.0F, 4.0F, 12.0F, 8.0F, 12.0F);
    private final String modelId;

    @Environment(EnvType.CLIENT)
    public BedrockEntityModel<?> model;

    public MarketablePlushieBlock(ABlockSettings settings, String modelId) {
        super(settings, new BlockWithEntityBehavior.Ticking(MarketablePlushieBlockEntity::new));
        this.modelId = modelId;
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(ROTATION, 0)
                .with(STACKED, false));
    }

    @Environment(EnvType.CLIENT)
    public BedrockModelReference getModel() {
        return new BedrockModelReference(AmbleKit.MOD_ID, this.modelId);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MarketablePlushieBlockEntity(PlushieBlockEntities.MARKETABLE_PLUSHIE_BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();

        boolean sameAbove = world.getBlockState(pos.up()).isOf(this);

        return this.getDefaultState()
                .with(ROTATION, RotationPropertyHelper.fromYaw(ctx.getPlayerYaw()))
                .with(STACKED, sameAbove);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(ROTATION, rotation.rotate(state.get(ROTATION), MAX_ROTATIONS));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(ROTATION, mirror.mirror(state.get(ROTATION), MAX_ROTATIONS));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, STACKED);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient) return;

        boolean sameAbove = world.getBlockState(pos.up()).isOf(this);
        if (state.get(STACKED) != sameAbove) {
            world.setBlockState(pos, state.with(STACKED, sameAbove), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, net.minecraft.item.ItemStack itemStack) {
        if (world.isClient) return;
        boolean sameAbove = world.getBlockState(pos.up()).isOf(this);
        if (state.get(STACKED) != sameAbove) {
            world.setBlockState(pos, state.with(STACKED, sameAbove), Block.NOTIFY_LISTENERS);
        }

        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        if (belowState.isOf(this)) {
            boolean belowSameAbove = world.getBlockState(below.up()).isOf(this);
            world.setBlockState(below, belowState.with(STACKED, belowSameAbove), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) return;

        if (!world.isClient) {
            BlockPos below = pos.down();
            BlockState belowState = world.getBlockState(below);
            if (belowState.isOf(this)) {
                boolean belowSameAbove = world.getBlockState(below.up()).isOf(this);
                world.setBlockState(below, belowState.with(STACKED, belowSameAbove), Block.NOTIFY_LISTENERS);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}