package dev.amble.lib.test.sync.handler;

import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.properties.bool.BoolProperty;
import dev.amble.lib.api.sync.properties.bool.BoolValue;
import dev.amble.lib.test.KitTestMod;
import dev.amble.lib.test.sync.server.ExampleServerSyncManager;

public class FirstExampleComponent extends KeyedSyncComponent {
    private static final BoolProperty IS_AWESOME_PROPERTY = new BoolProperty("is_awesome", false, ExampleServerSyncManager.getInstance());
    private final BoolValue isAwesome = IS_AWESOME_PROPERTY.create(this);

    public FirstExampleComponent() {
        super(ExampleComponentRegistry.Id.FIRST);
    }

    @Override
    public void onLoaded() {
        super.onLoaded();

        KitTestMod.LOGGER.info("FirstExampleComponent loaded");

        isAwesome.of(this, IS_AWESOME_PROPERTY);
    }

    public BoolValue isAwesome() {
        return isAwesome;
    }
}
