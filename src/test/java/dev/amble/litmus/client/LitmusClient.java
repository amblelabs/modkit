package dev.amble.litmus.client;

import dev.amble.lib.animation.client.BedrockBlockEntityRenderer;
import dev.amble.lib.animation.client.BedrockEntityRenderer;
import dev.amble.litmus.block.entity.LitmusBlockEntityTypes;
import dev.amble.litmus.commands.TestScreenCommand;
import dev.amble.litmus.entity.LitmusEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class LitmusClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
			TestScreenCommand.register(dispatcher);
		});
	}
}
