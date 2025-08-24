package dev.amble.lib.client;

import dev.amble.lib.client.bedrock.BedrockAnimationRegistry;
import dev.amble.lib.client.bedrock.BedrockModel;
import dev.amble.lib.client.bedrock.BedrockModelRegistry;
import dev.amble.lib.register.AmbleRegistries;
import dev.drtheo.scheduler.client.SchedulerClientMod;
import mc.duzo.animation.client.DuzoAnimationClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import dev.amble.lib.api.AmbleKitClientInitializer;

public class AmbleKitClient {
    public void onInitializeClient() {
        AmbleRegistries.getInstance().registerAll(
                BedrockModelRegistry.getInstance(),
                BedrockAnimationRegistry.getInstance()
        );
    }
}
