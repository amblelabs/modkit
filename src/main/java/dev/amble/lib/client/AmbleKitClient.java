package dev.amble.lib.client;

import dev.amble.lib.client.bedrock.BedrockAnimationRegistry;
import dev.amble.lib.client.bedrock.BedrockModelRegistry;
import dev.amble.lib.register.AmbleRegistries;
import dev.drtheo.scheduler.client.SchedulerClientMod;
import mc.duzo.animation.client.DuzoAnimationClient;

public class AmbleKitClient {
    public void onInitializeClient() {
        new SchedulerClientMod().onInitializeClient();
        new DuzoAnimationClient().onInitializeClient();

        AmbleRegistries.getInstance().registerAll(
                BedrockModelRegistry.getInstance(),
                BedrockAnimationRegistry.getInstance()
        );
    }
}
