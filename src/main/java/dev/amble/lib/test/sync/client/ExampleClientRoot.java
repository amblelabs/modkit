package dev.amble.lib.test.sync.client;

import java.util.UUID;

import net.minecraft.client.MinecraftClient;

import dev.amble.lib.api.sync.Exclude;
import dev.amble.lib.api.sync.manager.SyncManager;
import dev.amble.lib.api.sync.manager.client.ClientComponentData;
import dev.amble.lib.api.sync.manager.client.ClientRootComponent;
import dev.amble.lib.test.sync.ExampleRoot;

public class ExampleClientRoot extends ExampleRoot implements ClientRootComponent {
    @Exclude
    private final ClientComponentData data = new ClientComponentData();

    protected ExampleClientRoot(UUID uuid) {
        super(uuid);
    }

    @Override
    public SyncManager<ExampleClientRoot, MinecraftClient> getSyncManager() {
        return null;
    }

    @Override
    public ClientComponentData data() {
        return data;
    }
}
