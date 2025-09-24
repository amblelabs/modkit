package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.client.AnimationMetadata;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemFeatureRendererMixin {
	@Inject(method="renderItem", at = @At("HEAD"), cancellable = true)
	private void amblekit$renderItem(LivingEntity entity,
	                                 ItemStack stack,
	                                 ModelTransformationMode transformationMode,
	                                 Arm arm,
	                                 MatrixStack matrices,
	                                 VertexConsumerProvider vertexConsumers,
	                                 int light, CallbackInfo ci) {
		if (!(entity instanceof AnimatedEntity animated)) return;

		AnimationMetadata metadata = AnimationMetadata.getFor(animated);
		if (metadata == null || !metadata.hideHandItems()) return;

		ci.cancel();
	}
}
