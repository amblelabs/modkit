
package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.client.AnimationMetadata;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Final
	@Shadow
	private BufferBuilderStorage bufferBuilders;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V", ordinal = 0))
	public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (!(camera.getFocusedEntity() instanceof AnimatedEntity animated)) return;

		BedrockAnimation anim = BedrockAnimation.getFor(animated);
		if (anim == null) return;
		AnimationMetadata metadata = anim.metadata;
		if (metadata == null || !metadata.fpsCamera()) return;


		boolean thirdPerson = camera.isThirdPerson();
		boolean hasCamera = anim.boneTimelines.containsKey("camera");
		boolean isNear = camera.getPos().distanceTo(camera.getFocusedEntity().getPos().add(0, camera.getFocusedEntity().getStandingEyeHeight(), 0)) <= BedrockAnimation.HEAD_HIDE_DISTANCE;

		Vec3d vec3d = camera.getPos();
		double d = vec3d.x;
		double e = vec3d.y;
		double f = vec3d.z;
		VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
		BedrockAnimation.IS_RENDERING_PLAYER = true;
		BedrockAnimation.IS_RENDERING_HEAD = thirdPerson && hasCamera && !isNear;
		this.renderEntity(camera.getFocusedEntity(), d, e, f, tickDelta, matrices, immediate);
		BedrockAnimation.IS_RENDERING_PLAYER = false;
	}

	@Shadow
	private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
	                          MatrixStack matrices, VertexConsumerProvider vertexConsumers) {

	}
}
