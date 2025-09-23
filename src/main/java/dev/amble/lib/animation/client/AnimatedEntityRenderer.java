package dev.amble.lib.animation.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.AnimationState;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public interface AnimatedEntityRenderer<T extends AnimatedEntity> {
	/**
	 * @return the root modelpart of the renderer
	 */
	ModelPart getPart();

	default Optional<ModelPart> getChild(String name) {
		if (name.equals("root") || name.equalsIgnoreCase("player")) {
			return Optional.of(this.getPart());
		}
		return this.getPart().traverse().filter(part -> part.hasChild(name)).findFirst().map(part -> part.getChild(name));
	}

	default void applyAnimation(T entity, float progress) {
		BedrockAnimationReference reference = entity.getCurrentAnimation();
		if (reference == null) return;

		BedrockAnimation animation = reference.get().orElse(null);
		if (animation == null) return;

		AnimationState state = entity.getAnimationState();
		if (animation.isFinished(state)) return;

		if (entity.isAnimationDirty()) {
			state.stop();
		}

		state.startIfNotRunning(entity.getAge());

		animation.resetBones(this.getPart(), animation.getRunningSeconds(state, progress, 1.0F));
		animation.apply(this.getPart(), state, progress, 1.0F);
	}
}