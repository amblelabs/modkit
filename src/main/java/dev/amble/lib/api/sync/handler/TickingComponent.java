package dev.amble.lib.api.sync.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

public interface TickingComponent {
    default void tick(MinecraftServer server) { }

    @Environment(EnvType.CLIENT)
    default void tick(MinecraftClient client) { }
}
