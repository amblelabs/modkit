package dev.amble.litmus.block.entity.impl;

import dev.amble.lib.animation.AnimatedBlockEntity;
import dev.amble.lib.blockentity.ABlockEntity;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import dev.amble.litmus.LitmusMod;
import dev.amble.litmus.block.entity.LitmusBlockEntityTypes;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TestBlockEntity extends ABlockEntity implements AnimatedBlockEntity {
	private static final BedrockModelReference MODEL = new BedrockModelReference(LitmusMod.MOD_ID, "test_block");

	@Getter
	private final AnimationState animationState = new AnimationState();

	@Getter
	private int age = 0;

	public TestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public TestBlockEntity(BlockPos pos, BlockState state) {
		this(LitmusBlockEntityTypes.TEST_BLOCK, pos, state);
	}

	@Override
	public String getModId() {
		return LitmusMod.MOD_ID;
	}

	@Override
	public String getTexturePrefix() {
		return "block";
	}

	@Override
	public @Nullable BedrockModelReference getModel() {
		return MODEL;
	}

	@Override
	public boolean hasEmission() {
		return true;
	}

	@Override
	public void tick(World world, BlockPos pos, BlockState state) {
		age++;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;

		this.playAnimation(new BedrockAnimationReference("test_block", "use"));
		return ActionResult.SUCCESS;
	}
}
