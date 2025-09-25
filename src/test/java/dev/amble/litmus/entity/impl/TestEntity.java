package dev.amble.litmus.entity.impl;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.BedrockModelProvider;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import dev.amble.lib.skin.PlayerSkinTexturable;
import dev.amble.litmus.LitmusMod;
import dev.drtheo.scheduler.api.TimeUnit;
import dev.drtheo.scheduler.api.common.Scheduler;
import dev.drtheo.scheduler.api.common.TaskStage;
import lombok.Getter;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TestEntity extends PathAwareEntity implements AnimatedEntity, PlayerSkinTexturable {
	private static final BedrockModelReference MODEL = new BedrockModelReference(LitmusMod.MOD_ID, "test_entity");

	@Getter
	private final AnimationState animationState = new AnimationState();

	public TestEntity(EntityType<? extends TestEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public String getTexturePrefix() {
		return "entity";
	}

	@Override
	public @Nullable BedrockModelReference getModel() {
		return MODEL;
	}

	@Override
	public @Nullable Identifier getTexture() {
		if (this.getSkin() != null) return this.getSkinTexture();

		return AnimatedEntity.super.getTexture();
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (!player.getWorld().isClient()) {
			playAnimation(new BedrockAnimationReference("test_entity", "kneel_start"));

			Scheduler.get().runTaskLater(() -> {
				playAnimation(new BedrockAnimationReference("test_entity", "kneel_end"));
			}, TaskStage.END_SERVER_TICK, TimeUnit.SECONDS, (long) 7.54);
		}

		return super.interactMob(player, hand);
	}
}
