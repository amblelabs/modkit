package dev.amble.lib.animation;

import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityLike;
import org.jetbrains.annotations.Nullable;

public interface AnimatedEntity extends EntityLike {
	default void playAnimation(BedrockAnimationReference animation) {
		getAnimationState().start(this.getAge());
		AnimationTracker.getInstance().add(this.getUuid(), animation);
	}

	default BedrockAnimationReference getCurrentAnimation() {
		return AnimationTracker.getInstance().get(this);
	}

	default int getAge() {
		if (this instanceof Entity entity) {
			return entity.age;
		}

		throw new UnsupportedOperationException("getAge() is only supported for Entity instances. Override this method");
	}

	default boolean isAnimationDirty() {
		return AnimationTracker.getInstance().isDirty(this);
	}

	@Nullable
	static AnimatedEntity getInstance(EntityLike entity) {
		if (entity instanceof AnimatedEntity animated) {
			return animated;
		}
		return null;
	}

	AnimationState getAnimationState();
}
