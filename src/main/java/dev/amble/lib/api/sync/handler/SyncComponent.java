package dev.amble.lib.api.sync.handler;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import dev.amble.lib.api.sync.Disposable;
import dev.amble.lib.api.sync.Exclude;
import dev.amble.lib.api.sync.Initializable;
import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.data.CachedDirectedGlobalPos;
import dev.amble.lib.data.enummap.Ordered;

/**
 * Base class for all syncing components.
 *
 * @implNote There should be NO logic run in the constructor. If you need to
 * have such logic, implement it in an appropriate init method!
 */
public abstract class SyncComponent extends Initializable<SyncComponent.InitContext> implements Disposable {
    @Exclude
    protected RootComponent parent;
    @Exclude(strategy = Exclude.Strategy.NETWORK) private final IdLike id;

    /**
     * Do NOT under any circumstances run logic in this constructor. Default field
     * values should be inlined. All logic should be done in an appropriate init
     * method.
     *
     * @implNote The {@link SyncComponent#parent()} will always be null at the
     * time this constructor gets called.
     */
    public SyncComponent(IdLike id) {
        this.id = id;
    }

    public void postInit(InitContext ctx) {
    }

    public static void init(SyncComponent component, RootComponent parent, InitContext context) {
        component.setParent(parent);
        component.init(context);
    }
    public static <P extends RootComponent> void postInit(SyncComponent component, InitContext context) {
        component.postInit(context);
    }

    public RootComponent parent() {
        return this.parent;
    }
    public void setParent(RootComponent parent) {
        this.parent = parent;
    }

    @Override
    public void dispose() {
        this.parent = null;
    }

    public IdLike getId() {
        return this.id;
    }

    public interface IdLike extends Ordered {
        Class<? extends SyncComponent> clazz();

        default void set(RootComponent parent, SyncComponent component) {
            parent.getHandlers().set(component);
        }

        default SyncComponent get(RootComponent parent) {
            return parent.handler(this);
        }

        SyncComponent create();

        boolean creatable();

        String name();

        int index();

        void index(int i);
    }

    public static class AbstractId implements IdLike {

        private final String name;
        private final Supplier<SyncComponent> creator;
        private final Class<SyncComponent> clazz;

        private int index;

        public AbstractId(String name, Supplier<SyncComponent> creator, Class<SyncComponent> clazz) {
            this.name = name;
            this.creator = creator;
            this.clazz = clazz;
        }

        @Override
        public Class<SyncComponent> clazz() {
            return this.clazz;
        }

        @Override
        public SyncComponent create() {
            return this.creator.get();
        }

        @Override
        public boolean creatable() {
            return true;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public int index() {
            return this.index;
        }

        @Override
        public void index(int i) {
            this.index = i;
        }
    }

    public record InitContext(@Nullable CachedDirectedGlobalPos pos,
                              boolean deserialized) implements Initializable.Context {

        public static InitContext createdAt(@Nullable CachedDirectedGlobalPos pos) {
            return new InitContext(pos, false);
        }

        public static InitContext deserialize() {
            return new InitContext(null, true);
        }

        @Override
        public boolean created() {
            return !deserialized;
        }
    }
}
