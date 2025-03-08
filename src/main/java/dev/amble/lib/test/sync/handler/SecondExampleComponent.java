package dev.amble.lib.test.sync.handler;

import dev.amble.lib.api.sync.handler.KeyedSyncComponent;
import dev.amble.lib.api.sync.properties.bool.BoolProperty;
import dev.amble.lib.api.sync.properties.bool.BoolValue;
import dev.amble.lib.test.KitTestMod;
import dev.amble.lib.test.sync.server.ExampleServerSyncManager;

public class SecondExampleComponent extends KeyedSyncComponent {
    private static final BoolProperty IS_EPIC_PROPERTY = new BoolProperty("is_epic", false, ExampleServerSyncManager.getInstance());
    private final BoolValue isEpic = IS_EPIC_PROPERTY.create(this);


    public SecondExampleComponent() {
        super(ExampleComponentRegistry.Id.SECOND);
    }

    @Override
    public void onLoaded() {
        super.onLoaded();

        KitTestMod.LOGGER.info("SecondExampleComponent loaded");

        isEpic.of(this, IS_EPIC_PROPERTY);
    }

    public BoolValue isEpic() {
        return isEpic;
    }
}
