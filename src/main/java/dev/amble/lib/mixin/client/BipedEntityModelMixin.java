package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.client.AnimatedEntityModel;
import dev.amble.lib.animation.client.AnimationMetadata;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> implements AnimatedEntityModel {
	@Unique
	ModelPart root;

	@Shadow
	@Final
	public ModelPart head;

	@Inject(method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	private void animation$setAnglePre(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		if (!(livingEntity instanceof AnimatedEntity player)) return;

		this.applyAnimationPre(livingEntity, h);
	}

	@Inject(method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;copyTransform(Lnet/minecraft/client/model/ModelPart;)V"))
	private void animation$setAngle(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		if (!(livingEntity instanceof AnimatedEntity player)) return;

		this.applyAnimation(livingEntity, h);

		if (!BedrockAnimation.isRenderingPlayer || livingEntity != MinecraftClient.getInstance().cameraEntity) {
			head.visible = true;
			return;
		}

		head.visible = false;
	}

	@Inject(method = "<init>(Lnet/minecraft/client/model/ModelPart;Ljava/util/function/Function;)V", at = @At("TAIL"))
	public void animation$init(ModelPart root, Function<Identifier, RenderLayer> renderLayerFactory, CallbackInfo ci) {
		this.root = root;
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}
}
