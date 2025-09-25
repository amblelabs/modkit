package dev.amble.lib.animation.client;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.animation.AnimatedInstance;
import dev.amble.lib.animation.AnimationTracker;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public interface AnimatedEntityModel {
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
	default void applyAnimation(AnimatedInstance entity, float progress) {
		BedrockAnimationReference reference = entity.getCurrentAnimation();

		if (reference == null) return;

		BedrockAnimation animation = reference.get().orElse(null);
		if (animation == null) {
			AmbleKit.LOGGER.error("Got unknown animation reference: {}", reference.id());
			AnimationTracker.getInstance().removeLocal(entity);
			return;
		}

		AnimationState state = entity.getAnimationState();

		if (entity.isAnimationDirty()) {
			state.stop();
			state.startIfNotRunning(entity.getAge());
		}

		if (animation.isFinished(state)) {
			AnimationTracker.getInstance().removeLocal(entity);
			return;
		}

		state.startIfNotRunning(entity.getAge());

		animation.apply(this.getPart(), state, progress, 1.0F, entity);
	}

	default void applyAnimationPre(AnimatedInstance entity, float progress) {
		BedrockAnimationReference reference = entity.getCurrentAnimation();

		if (reference == null) {
			this.getPart().traverse().forEach(ModelPart::resetTransform);
			return;
		}

		BedrockAnimation animation = reference.get().orElse(null);
		if (animation == null) {
			this.getPart().traverse().forEach(ModelPart::resetTransform);
			return;
		}

		AnimationState state = entity.getAnimationState();
		if (state == null || animation.isFinished(state)) {
			this.getPart().traverse().forEach(ModelPart::resetTransform);
			return;
		}

		if (animation.metadata != null && !animation.metadata.movement()) {
			this.getPart().traverse().forEach(ModelPart::resetTransform);
		}
	}
}