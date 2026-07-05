package dev.amble.plushies;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.animation.AnimatedBlockEntity;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.AnimationState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MarketablePlushieBlockEntity extends BlockEntity implements AnimatedBlockEntity, BlockEntityTicker<MarketablePlushieBlockEntity> {

    @Getter
    private final AnimationState animationState = new AnimationState();

    private int age = 0;

    public MarketablePlushieBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MarketablePlushieBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(PlushieBlockEntities.MARKETABLE_PLUSHIE_BLOCK_ENTITY_TYPE, blockPos, blockState);
    }

    @Override
    public int getAge() {
        return this.age;
    }

    @Override
    public AnimationState getAnimationState() {
        return animationState;
    }

    @Override
    public @Nullable BedrockModelReference getModel() {
        Block block = this.getCachedState().getBlock();
        if (block instanceof MarketablePlushieBlock plushieBlock) {
            return new BedrockModelReference(AmbleKit.MOD_ID, plushieBlock.getModelId());
        }
        return null;
    }

    @Override
    public float getRenderYaw() {
        return this.getCachedState().get(MarketablePlushieBlock.ROTATION) * 22.5f;
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, MarketablePlushieBlockEntity blockEntity) {
        this.age++;
    }
}
