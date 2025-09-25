package dev.amble.litmus.client;

import dev.amble.lib.animation.client.BedrockBlockEntityRenderer;
import dev.amble.lib.animation.client.BedrockEntityRenderer;
import dev.amble.litmus.block.entity.LitmusBlockEntityTypes;
import dev.amble.litmus.entity.LitmusEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class LitmusClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(LitmusBlockEntityTypes.TEST_BLOCK, BedrockBlockEntityRenderer::new);
		EntityRendererRegistry.register(LitmusEntities.TEST_ENTITY, BedrockEntityRenderer::new);
	}
}
