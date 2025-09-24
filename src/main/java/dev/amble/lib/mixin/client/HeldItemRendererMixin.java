package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.client.AnimationMetadata;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
	@Inject(method="renderFirstPersonItem", at=@At("HEAD"), cancellable = true)
	private void amblekit$renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		AnimationMetadata metadata = AnimationMetadata.getFor((dev.amble.lib.animation.AnimatedEntity) player);
		if (metadata == null || !metadata.fpsCamera()) return;

		ci.cancel();
	}
}
