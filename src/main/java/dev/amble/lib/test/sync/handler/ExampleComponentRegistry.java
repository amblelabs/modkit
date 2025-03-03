package dev.amble.lib.test.sync.handler;

import java.util.function.Supplier;

import dev.amble.lib.api.sync.handler.ComponentManager;
import dev.amble.lib.api.sync.handler.ComponentRegistry;
import dev.amble.lib.api.sync.handler.SyncComponent;
import dev.amble.lib.test.sync.server.ExampleServerSyncManager;

public class ExampleComponentRegistry extends ComponentRegistry {
    private static final ExampleComponentRegistry instance = new ExampleComponentRegistry();

    public static ExampleComponentRegistry getInstance() {
        return instance;
    }

    @Override
    protected SyncComponent.IdLike[] ids() {
        return Id.values();
    }

    public enum Id implements SyncComponent.IdLike {
        HANDLERS(ComponentManager.class, () -> new ComponentManager(ExampleServerSyncManager.getInstance())),
        FIRST(FirstExampleComponent.class, FirstExampleComponent::new),
        SECOND(SecondExampleComponent.class, SecondExampleComponent::new);

        private final Supplier<SyncComponent> creator;

        private final Class<? extends SyncComponent> clazz;

        private Integer index = null;

        @SuppressWarnings("unchecked")
        <T extends SyncComponent> Id(Class<T> clazz, Supplier<T> creator) {
            this.clazz = clazz;
            this.creator = (Supplier<SyncComponent>) creator;
        }

        @Override
        public Class<? extends SyncComponent> clazz() {
            return clazz;
        }

        @Override
        public SyncComponent create() {
            return this.creator.get();
        }

        @Override
        public boolean creatable() {
            return this.creator != null;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public void index(int i) {
            this.index = i;
        }
    }
}
