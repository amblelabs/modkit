package dev.amble.lib.client;

import dev.drtheo.scheduler.client.SchedulerClientMod;
import mc.duzo.animation.client.DuzoAnimationClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import dev.amble.lib.api.AmbleKitClientInitializer;

public class AmbleKitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new DuzoAnimationClient().onInitializeClient();
        new SchedulerClientMod().onInitializeClient();

        FabricLoader.getInstance().invokeEntrypoints("amblekit-client", AmbleKitClientInitializer.class,
                AmbleKitClientInitializer::onInitialize);
    }
}
