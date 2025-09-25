package dev.amble.lib.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.entity.EntityType;

public class RegistrationUtil {
	// if i put this in the interface it crashes cus it cant load that stuff
	@Environment(EnvType.CLIENT)
	public static void registerBedrockRenderer(EntityType<?> type) {
		EntityRendererRegistry.register(type, ctx -> new dev.amble.lib.animation.client.BedrockEntityRenderer(ctx));
	}
}
