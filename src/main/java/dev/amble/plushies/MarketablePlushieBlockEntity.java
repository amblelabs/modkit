package dev.amble.plushies;

import dev.amble.lib.animation.AnimatedBlockEntity;
import dev.amble.lib.blockentity.ABlockEntity;
import dev.amble.lib.client.bedrock.*;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MarketablePlushieBlockEntity extends ABlockEntity implements AnimatedBlockEntity {

    private static final BedrockAnimationReference ANIMATION_REFERENCE = new BedrockAnimationReference("squish", "squish");

    @Getter
    private final AnimationState animationState = new AnimationState();

    public MarketablePlushieBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MarketablePlushieBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(PlushieBlockEntities.MARKETABLE_PLUSHIE_BLOCK_ENTITY_TYPE, blockPos, blockState);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getAge() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null ? client.player.age : 0;
    }

    @Override
    public AnimationState getAnimationState() {
        return animationState;
    }

    @Override
    public String getTexturePrefix() {
        return "block";
    }

    @Override
    public @Nullable BedrockModelReference getModel() {
        Block block = this.getCachedState().getBlock();
        if (block instanceof MarketablePlushieBlock plushieBlock) {
            return plushieBlock.getModelReference();
        }
        return null;
    }

    @Override
    public float getRenderYaw() {
        return this.getCachedState().get(MarketablePlushieBlock.ROTATION) * 22.5f;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        world.playSound(null, pos, PlushieSounds.BOOP, SoundCategory.BLOCKS, 0.4f, world.getRandom().nextBoolean() ? 1.0f : 0.9f);
        this.playAnimation(ANIMATION_REFERENCE);
        return ActionResult.SUCCESS;
    }
}
