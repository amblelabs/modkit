package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.client.AnimationMetadata;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void animation$tickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (!(player instanceof AnimatedEntity animated)) return;

		AnimationMetadata metadata = AnimationMetadata.getFor(animated);

        if (metadata == null || metadata.movement()) return;

        ci.cancel();
    }

}