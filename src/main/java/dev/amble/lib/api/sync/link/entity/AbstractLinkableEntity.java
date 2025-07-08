package dev.amble.lib.api.sync.link.entity;

import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.api.sync.link.Linkable;
import dev.amble.lib.api.sync.link.RootRef;
import dev.amble.lib.api.sync.manager.SyncManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public interface AbstractLinkableEntity<R extends RootComponent> extends Linkable<R> {

    World getWorld();

    DataTracker getDataTracker();

    TrackedData<Optional<UUID>> getTracked();

    RootRef<R> asRef();

    void setRef(RootRef<R> ref);

    SyncManager getSyncManager();

    @Override
    default void link(UUID id) {
        this.setRef(RootRef.createAs(this.getWorld(), id, this.getSyncManager()));
        this.getDataTracker().set(this.getTracked(), Optional.ofNullable(id));
    }

    @Override
    default void link(R tardis) {
        this.setRef(RootRef.createAs(this.getWorld(), tardis, this.getSyncManager()));
        this.getDataTracker().set(this.getTracked(), Optional.of(tardis.getUuid()));
    }

    @Override
    default RootRef<R> parent() {
        RootRef<R> result = this.asRef();

        if (result == null) {
            this.link(this.getDataTracker().get(this.getTracked()).orElse(null));
            return this.parent();
        }

        return result;
    }

    default void initDataTracker() {
        this.getDataTracker().startTracking(this.getTracked(), Optional.empty());
    }

    default void onTrackedDataSet(TrackedData<?> data) {
        if (!this.getTracked().equals(data))
            return;

        this.link(this.getDataTracker().get(this.getTracked()).orElse(null));
    }

    default void readCustomDataFromNbt(NbtCompound nbt) {
        NbtElement id = nbt.get(getNbtPath());

        if (id == null)
            return;

        this.link(NbtHelper.toUuid(id));

        if (this.getWorld() == null)
            return;

        this.onLinked();
    }

    default void writeCustomDataToNbt(NbtCompound nbt) {
        RootRef<R> ref = this.asRef();

        if (ref != null && ref.getId() != null)
            nbt.putUuid(getNbtPath(), ref.getId());
    }

    default String getNbtPath() {
        return this.getSyncManager().createPacket("root").getPath();
    }

    static <T extends Entity & AbstractLinkableEntity<RootComponent>> TrackedData<Optional<UUID>> register(Class<T> self) {
        return DataTracker.registerData(self, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    }
}
