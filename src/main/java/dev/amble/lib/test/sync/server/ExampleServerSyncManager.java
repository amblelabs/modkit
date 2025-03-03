package dev.amble.lib.test.sync.server;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

import net.minecraft.server.network.ServerPlayerEntity;

import dev.amble.lib.api.sync.handler.ComponentRegistry;
import dev.amble.lib.api.sync.handler.SyncComponent;
import dev.amble.lib.api.sync.manager.server.ServerSyncManager;
import dev.amble.lib.test.KitTestMod;
import dev.amble.lib.test.sync.handler.ExampleComponentRegistry;
import dev.amble.lib.util.ServerLifecycleHooks;

public class ExampleServerSyncManager extends ServerSyncManager<ExampleServerRoot> {
    private static ExampleServerSyncManager instance;

    public static void init() {
        instance = new ExampleServerSyncManager();
    }

    @Override
    public Set<ServerPlayerEntity> getSubscribedPlayers(ExampleServerRoot root) {
        return new HashSet<>(PlayerLookup.all(ServerLifecycleHooks.get()));
    }

    @Override
    protected Class<ExampleServerRoot> getRootComponentType() {
        return ExampleServerRoot.class;
    }

    @Override
    public ComponentRegistry getRegistry() {
        return ExampleComponentRegistry.getInstance();
    }

    @Override
    public SyncComponent.IdLike getHandlersId() {
        return ExampleComponentRegistry.Id.HANDLERS;
    }

    @Override
    public String modId() {
        return KitTestMod.MOD_ID;
    }

    @Override
    public String name() {
        return "example";
    }
    public static ExampleServerSyncManager getInstance() {
        return instance;
    }
}
