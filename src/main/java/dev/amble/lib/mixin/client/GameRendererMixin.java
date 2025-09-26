package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method="render", at=@At("HEAD"))
	private void amble$renderGame(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player != null) {
			BedrockAnimation anim = BedrockAnimation.getFor((AnimatedEntity) player);
			Optional<Boolean> wasHudHidden = BedrockAnimation.wasHudHidden;

			System.out.println("anim: " + anim + " | wasHudHidden: " + wasHudHidden + " | hudHidden: " + MinecraftClient.getInstance().options.hudHidden);
			if (anim != null) {
				if (wasHudHidden.isEmpty()) { // start
					wasHudHidden = Optional.of(MinecraftClient.getInstance().options.hudHidden);

					if (anim.metadata.hideHud()) {
						MinecraftClient.getInstance().options.hudHidden = true;
						BedrockAnimation.wasHudHidden = wasHudHidden;
					}
				}
			} else {
				if (wasHudHidden.isPresent()) { // end
					MinecraftClient.getInstance().options.hudHidden = wasHudHidden.get();
					wasHudHidden = Optional.empty();
					BedrockAnimation.wasHudHidden = wasHudHidden;
				}
			}}
	}
}
