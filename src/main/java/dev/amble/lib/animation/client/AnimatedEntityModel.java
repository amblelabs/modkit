package dev.amble.lib.animation.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public interface AnimatedEntityModel<T extends Entity & AnimatedEntity> {
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

	/**
	 * Call this in {@link EntityModel#setAngles(Entity, float, float, float, float, float)} where progress is usually named 'h'
	 */
	default void applyAnimation(T entity, float progress) {
		BedrockAnimationReference reference = entity.getCurrentAnimation();
		if (reference == null) return;

		BedrockAnimation animation = reference.get().orElse(null);
		if (animation == null) return;

		AnimationState state = entity.getAnimationState();

		if (entity.isAnimationDirty()) {
			state.stop();
			state.startIfNotRunning(entity.getAge());
		}

		if (animation.isFinished(state)) return;

		state.startIfNotRunning(entity.getAge());

		animation.apply(this.getPart(), state, progress, 1.0F, entity);
	}
}