package dev.amble.lib.animation.client;

import com.google.gson.JsonObject;
import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import lombok.Getter;
import lombok.With;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.AnimationState;
import org.jetbrains.annotations.Nullable;

/**
 * Metadata for animations, controlling how they behave in certain situations.
 * in "filename.metadata.json"
 * @param movement Whether the animation should allow player movement. Default: true
 * @param perspective The perspective the animation should play in. Default: null (all perspectives)
 * @param fpsCamera Whether the animation should have FPS camera controls. Default: true
 * @param excess Any excess metadata not used by AmbleKit.
 */
@Environment(EnvType.CLIENT)
public record AnimationMetadata(@With boolean movement, @With Perspective perspective, @With boolean fpsCamera, @With boolean hideHandItems, @With
                                JsonObject excess) {
	public static final AnimationMetadata DEFAULT = new AnimationMetadata(true, null, true, true, new JsonObject());

	@Nullable
	public static AnimationMetadata getFor(AnimatedEntity animated) {
		BedrockAnimationReference ref = animated.getCurrentAnimation();
		if (ref == null) return null;

		BedrockAnimation anim = ref.get().orElse(null);
		if (anim == null) return null;
		AnimationState state = animated.getAnimationState();
		if (state == null || anim.isFinished(state)) return null;

		AnimationMetadata metadata = anim.metadata;
		return metadata;
	}

	public enum Perspective {
		FIRST_PERSON(true, false),
		THIRD_PERSON_BACK(false, false),
		THIRD_PERSON_FRONT(false, true);
		@Getter
		private final boolean firstPerson;
		@Getter
		private final boolean frontView;

		Perspective(boolean firstPerson, boolean frontView) {
			this.firstPerson = firstPerson;
			this.frontView = frontView;
		}
	}
}
