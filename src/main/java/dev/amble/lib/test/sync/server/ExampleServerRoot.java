package dev.amble.lib.test.sync.server;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;

import dev.amble.lib.api.sync.Exclude;
import dev.amble.lib.api.sync.manager.SyncManager;
import dev.amble.lib.api.sync.manager.server.ServerComponentData;
import dev.amble.lib.api.sync.manager.server.ServerRootComponent;
import dev.amble.lib.test.sync.ExampleRoot;

public class ExampleServerRoot extends ExampleRoot implements ServerRootComponent {
    @Exclude(strategy = Exclude.Strategy.NETWORK)
    private final ServerComponentData data = new ServerComponentData();

    public ExampleServerRoot(UUID uuid) {
        super(uuid);
    }

    @Override
    public SyncManager<ExampleServerRoot, MinecraftServer> getSyncManager() {
        return ExampleServerSyncManager.getInstance();
    }

    @Override
    public ServerComponentData data() {
        return data;
    }
}
