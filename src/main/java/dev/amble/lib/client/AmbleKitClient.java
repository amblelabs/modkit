package dev.amble.lib.client;

import dev.amble.lib.animation.client.BedrockBlockEntityRenderer;
import dev.amble.lib.client.bedrock.BedrockAnimationRegistry;
import dev.amble.lib.client.bedrock.BedrockModel;
import dev.amble.lib.client.bedrock.BedrockModelRegistry;
import dev.amble.lib.register.AmbleRegistries;
import dev.amble.lib.skin.client.SkinGrabber;
import dev.amble.plushies.PlushieBlockEntities;
import dev.amble.plushies.client.MarketablePlushieRenderer;
import dev.drtheo.scheduler.client.SchedulerClientMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import dev.amble.lib.api.AmbleKitClientInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AmbleKitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().invokeEntrypoints("amblekit-client", AmbleKitClientInitializer.class,
                AmbleKitClientInitializer::onInitialize);

        AmbleRegistries.getInstance().registerAll(
                BedrockModelRegistry.getInstance(),
                BedrockAnimationRegistry.getInstance()
        );

	    ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			SkinGrabber.INSTANCE.tick();
	    });
	}
}
