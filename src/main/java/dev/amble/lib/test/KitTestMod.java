package dev.amble.lib.test;

import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.test.sync.server.ExampleServerSyncManager;

@TestOnly
public class KitTestMod implements ModInitializer {
    public static final String MOD_ID = "amblekit-test";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final boolean TEST_SYNCING = true;

    @Override
    public void onInitialize() {
        if (!(AmbleKit.isTestingEnabled())) return;

        LOGGER.info("AmbleKit Tests Enabled!");

        if (TEST_SYNCING) {
            initSyncing();
        }
    }

    private static void initSyncing() {
        LOGGER.info("Syncing Tests Enabled!");

        ExampleServerSyncManager.init();
    }
}
