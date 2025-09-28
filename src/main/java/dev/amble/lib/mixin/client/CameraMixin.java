package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

	@Shadow
	public abstract Vec3d getPos();

	@Shadow
	public abstract Quaternionf getRotation();

	@Shadow
	protected abstract double clipToSpace(double desiredCameraDistance);

	@Inject(method="update", at=@At("TAIL"))
	private void amble$update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (!(focusedEntity instanceof AnimatedEntity animated)) return;

		BedrockAnimationReference ref = animated.getCurrentAnimation();
		if (ref == null) return;

		BedrockAnimation animation = ref.get().orElse(null);
		if (animation == null || !animation.metadata.fpsCamera()) return;

		AnimationState state = animated.getAnimationState();
		if (state == null || animation.isFinished(state)) return;

		double progress = animation.getRunningSeconds(state);

		String cameraPart = thirdPerson ? "camera" : "head";
		if (!animation.boneTimelines.containsKey(cameraPart)) return;

		Vec3d rotation = animation.boneTimelines.get(cameraPart).rotation().resolve(progress);
		float yaw;

		if (thirdPerson && animation.metadata.fpsCameraCopiesHead()) {
			yaw = (focusedEntity instanceof ClientPlayerEntity clientPlayer) ? (MathHelper.lerpAngleDegrees(tickDelta, clientPlayer.prevHeadYaw, clientPlayer.headYaw)) : focusedEntity.getHeadYaw();
		} else {
			yaw = (focusedEntity instanceof ClientPlayerEntity clientPlayer) ? (MathHelper.lerpAngleDegrees(tickDelta, clientPlayer.prevBodyYaw, clientPlayer.bodyYaw)) : focusedEntity.getBodyYaw();
		}

		Vec3d position = animation.boneTimelines.get(cameraPart).position().resolve(progress);
		float height = cameraPart.equals("head") ? focusedEntity.getStandingEyeHeight() : 0;

		this.setPos(
				new Vec3d(
				MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX()),
				MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY()),
				MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ()))
		);

		Pair<Float, Float> rots = animation.getRotations(cameraPart, (float) progress);
		float animYaw = rots.getRight();
		float animPitch = rots.getLeft();

		if (thirdPerson) {
			Vec3d pos = position.rotateY((float)Math.toRadians(90)).multiply(-1 / 16F);
			this.setRotation(yaw, 0);
			this.moveBy(clipToSpace(pos.x), clipToSpace(pos.y), clipToSpace(pos.z));
			this.setRotation(animYaw + yaw, animPitch);
			return;
		}

		// head positioning, has some issues though
		this.setRotation(animYaw + yaw, animPitch);
		this.setPos(position.rotateY((float) -Math.toRadians(-yaw)).multiply(-1/16F).add(this.getPos()).add(0, height, 0));
	}
}
