package dev.amble.lib.test.client;

import net.fabricmc.api.ClientModInitializer;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.test.KitTestMod;
import dev.amble.lib.test.sync.client.ExampleClientSyncManager;

public class KitTestModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (!(AmbleKit.isTestingEnabled())) return;

        if (KitTestMod.TEST_SYNCING) {
            initSyncing();
        }
    }

    private static void initSyncing() {
        ExampleClientSyncManager.init();
    }
}
