package dev.amble.lib.mixin.client;

import dev.amble.lib.client.bedrock.BedrockAnimation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
	@Shadow
	@Final
	public ModelPart head;

	@Inject(method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;copyTransform(Lnet/minecraft/client/model/ModelPart;)V"))
	private void animation$setAngle(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		if (!BedrockAnimation.isRenderingPlayer || livingEntity != MinecraftClient.getInstance().cameraEntity) return;

		head.visible = false;
	}
}
