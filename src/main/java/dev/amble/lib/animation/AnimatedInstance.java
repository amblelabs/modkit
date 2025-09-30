package dev.amble.lib.animation;

import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.minecraft.entity.AnimationState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface AnimatedInstance extends BedrockModelProvider, EffectProvider {
	UUID getUuid();
	int getAge();
	AnimationState getAnimationState();

	default void playAnimation(BedrockAnimationReference animation) {
		getAnimationState().start(this.getAge());
		AnimationTracker.getInstance().add(this.getUuid(), animation);
	}

	@Nullable
	default BedrockAnimationReference getCurrentAnimation() {
		return AnimationTracker.getInstance().get(this);
	}

	default boolean isAnimationDirty() {
		return AnimationTracker.getInstance().isDirty(this);
	}
}
