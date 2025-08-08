package dev.amble.lib.test.sync;

import java.util.UUID;

import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.test.sync.handler.ExampleComponentRegistry;
import dev.amble.lib.test.sync.handler.FirstExampleComponent;
import dev.amble.lib.test.sync.handler.SecondExampleComponent;

public abstract class ExampleRoot extends RootComponent {
    protected ExampleRoot(UUID uuid) {
        super(uuid);
    }

    public FirstExampleComponent first() {
        return this.handler(ExampleComponentRegistry.Id.FIRST);
    }
    public SecondExampleComponent second() {
        return this.handler(ExampleComponentRegistry.Id.SECOND);
    }
}
