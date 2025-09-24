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
		if(camera.isThirdPerson() || !(camera.getFocusedEntity() instanceof AnimatedEntity animated)) return;

		AnimationMetadata metadata = AnimationMetadata.getFor(animated);
		if (metadata == null || !metadata.fpsCamera()) return;

		Vec3d vec3d = camera.getPos();
		double d = vec3d.x;
		double e = vec3d.y;
		double f = vec3d.z;
		VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
		BedrockAnimation.isRenderingPlayer = true;
		this.renderEntity(camera.getFocusedEntity(), d, e, f, tickDelta, matrices, (VertexConsumerProvider) immediate);
		BedrockAnimation.isRenderingPlayer = false;
	}

	@Shadow
	private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
	                          MatrixStack matrices, VertexConsumerProvider vertexConsumers) {

	}
}
