package dev.amble.lib.api.sync.handler;

import dev.amble.lib.api.sync.Exclude;
import dev.amble.lib.api.sync.properties.PropertyMap;
import dev.amble.lib.api.sync.properties.Value;

public abstract class KeyedSyncComponent extends SyncComponent {
    @Exclude(strategy = Exclude.Strategy.FILE)
    private PropertyMap data = new PropertyMap();

    /**
     * Do NOT under any circumstances run logic in this constructor. Default field
     * values should be inlined. All logic should be done in an appropriate init
     * method.
     *
     * @implNote The {@link SyncComponent#parent()} will always be null at the
     *           time this constructor gets called.
     */
    public KeyedSyncComponent(IdLike id) {
        super(id);
    }

    @Override
    protected void init(InitContext context) {
        if (this.data == null)
            this.data = new PropertyMap();

        super.init(context);
    }

    public void register(Value<?> property) {
        this.data.put(property.getProperty().getName(), property);
    }

    @Override
    public void dispose() {
        super.dispose();

        this.data.dispose();
    }

    public PropertyMap getPropertyData() {
        return data;
    }
}
