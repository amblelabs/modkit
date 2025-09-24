package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Shadow
	protected abstract void setPos(Vec3d pos);

	@Shadow
	protected abstract void moveBy(double x, double y, double z);

	@Shadow
	public abstract float getYaw();

	@Inject(method="update", at=@At("TAIL"))
	private void amble$update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (thirdPerson || !(focusedEntity instanceof AnimatedEntity animated)) return;

		BedrockAnimationReference ref = animated.getCurrentAnimation();
		if (ref == null) return;

		BedrockAnimation animation = ref.get().orElse(null);
		if (animation == null || !animation.metadata.fpsCamera()) return;

		AnimationState state = animated.getAnimationState();
		if (state == null || animation.isFinished(state)) return;

		double progress = animation.getRunningSeconds(state, animated.getAge() + tickDelta, 1.0F);

		Vec3d rotation = animation.boneTimelines.get("head").rotation().resolve(progress);
		this.setRotation((float) rotation.y + this.getYaw(), (float) rotation.x);

		Vec3d position = animation.boneTimelines.get("head").position().resolve(progress).multiply(1/16F)
				.rotateX((float)Math.toRadians(rotation.getX()))
				.rotateY((float)Math.toRadians(rotation.getY()))
				.rotateZ((float)Math.toRadians(rotation.getZ()));
		this.moveBy(position.x, position.y, position.z);
	}
}
