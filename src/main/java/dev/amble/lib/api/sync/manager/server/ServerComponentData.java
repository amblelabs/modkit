package dev.amble.lib.api.sync.manager.server;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import dev.amble.lib.api.sync.Exclude;
import dev.amble.lib.api.sync.handler.SyncComponent;

public class ServerComponentData {
    @Exclude
    private boolean removed;

    @Exclude
    private final Set<SyncComponent> delta = new HashSet<>(32);

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void markDirty(SyncComponent component) {
        if (component == null)
            return;

        if (!(component.parent() instanceof ServerRootComponent sParent))
            return;
        if (sParent.data() != this)
            return;

        this.delta.add(component);
    }

    public void consumeDelta(Consumer<SyncComponent> consumer) {
        if (this.delta.isEmpty())
            return;

        for (SyncComponent component : this.delta) {
            consumer.accept(component);
        }

        this.delta.clear();
    }

    public boolean hasDelta() {
        return !this.delta.isEmpty();
    }

    public int getDeltaSize() {
        return this.delta.size();
    }
}
