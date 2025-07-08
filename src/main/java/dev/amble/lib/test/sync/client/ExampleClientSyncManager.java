package dev.amble.lib.test.sync.client;

import dev.amble.lib.api.sync.handler.ComponentRegistry;
import dev.amble.lib.api.sync.handler.SyncComponent;
import dev.amble.lib.api.sync.manager.client.ClientSyncManager;
import dev.amble.lib.api.sync.manager.server.ServerSyncManager;
import dev.amble.lib.test.KitTestMod;
import dev.amble.lib.test.sync.handler.ExampleComponentRegistry;
import dev.amble.lib.test.sync.server.ExampleServerRoot;
import dev.amble.lib.test.sync.server.ExampleServerSyncManager;

public class ExampleClientSyncManager extends ClientSyncManager<ExampleClientRoot> {
    private static ExampleClientSyncManager instance;

    public static void init() {
        instance = new ExampleClientSyncManager();
    }

    public static ExampleClientSyncManager getInstance() {
        return instance;
    }

    @Override
    protected Class<ExampleClientRoot> getRootComponentType() {
        return ExampleClientRoot.class;
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

    @Override
    public ServerSyncManager<ExampleServerRoot> asServer() {
        return ExampleServerSyncManager.getInstance();
    }

    @Override
    public ClientSyncManager<ExampleClientRoot> asClient() {
        return this;
    }
}
