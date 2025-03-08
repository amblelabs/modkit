package dev.amble.lib.api.sync.manager.server;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.amble.lib.api.sync.Initializable;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import dev.amble.lib.api.KitEvents;
import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.api.sync.handler.SyncComponent;
import dev.amble.lib.api.sync.manager.SyncManager;
import dev.amble.lib.api.sync.properties.Value;
import dev.amble.lib.events.ServerCrashEvent;
import dev.amble.lib.events.WorldSaveEvent;

public abstract class ServerSyncManager<T extends RootComponent & ServerRootComponent> extends SyncManager<T, MinecraftServer> {
    protected final ComponentFileManager<T> fileManager;
    private final Set<T> delta = new HashSet<>();

    public ServerSyncManager() {
        this.fileManager = new ComponentFileManager<>(this.modId(), this.name(), this.getRootComponentType());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.fileManager.setLocked(false));
        ServerLifecycleEvents.SERVER_STOPPING.register(this::saveAndReset);

        ServerCrashEvent.EVENT.register(((server, report) -> this.reset())); // just panic and reset
        WorldSaveEvent.EVENT.register(world -> this.save(world.getServer(), false));

        KitEvents.SYNC_ROOT.register((player, chunk) -> {
            if (this.fileManager.isLocked()) return;

            if (this.lookup.size() >= 8) {
                this.sendBulk(player, new HashSet<>(this.lookup.values()));
                return;
            }

            this.sendAll(player, new HashSet<>(this.lookup.values()));
        });

        /*
        if (DEMENTIA) {
            TardisEvents.UNLOAD_TARDIS.register(WorldWithTardis.forDesync((player, tardisSet) -> {
                for (ServerTardis tardis : tardisSet) {
                    if (isInvalid(tardis))
                        continue;

                    this.sendTardisRemoval(player, tardis);
                }
            }));
        }*/


        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (T root : this.lookup.values()) {
                if (root.data().isRemoved())
                    continue;

                root.tick(server);
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (this.fileManager.isLocked())
                return;

            for (T tardis : new HashSet<>(this.delta)) {
                if (isInvalid(tardis))
                    continue;

                if (!tardis.data().hasDelta())
                    continue;

                PacketByteBuf buf = this.prepareSendDelta(tardis);
                tardis.data().consumeDelta(component -> this.writeComponent(component, buf));

                this.getSubscribedPlayers(tardis).forEach(
                        watching -> this.sendComponents(watching, buf)
                );
            }

            this.delta.clear();
        });
    }

    @Override
    public @Nullable T demand(MinecraftServer server, UUID uuid) {
        if (uuid == null)
            return null; // ugh - ong bro

        T result = this.lookup.get(uuid);

        if (result == null)
            result = this.load(server, uuid);

        return result;
    }

    @Override
    public void load(MinecraftServer server, UUID uuid, @Nullable Consumer<T> consumer) {
        if (consumer != null)
            consumer.accept(this.load(server, uuid));
    }

    private T load(MinecraftServer server, UUID uuid) {
        return this.fileManager.load(server, this, uuid, this::read, this.lookup::put);
    }

    public void loadAll(MinecraftServer server, @Nullable Consumer<T> consumer) {
        for (UUID id : this.fileManager.getList(server)) {
            this.get(server, id, consumer);
        }
    }

    public void remove(MinecraftServer server, T tardis) {
        tardis.data().setRemoved(true);

        tardis.dispose();
        this.sendRemoval(server, tardis);

        this.lookup.remove(tardis.getUuid());
        this.fileManager.delete(server, tardis.getUuid());
    }

    protected void sendRemoval(MinecraftServer server, T tardis) {
        if (tardis == null)
            return;

        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            this.sendRemoval(player, data);
        }
    }

    protected void sendRemoval(ServerPlayerEntity player, T tardis) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());

        this.sendRemoval(player, data);
    }

    protected void sendRemoval(ServerPlayerEntity player, PacketByteBuf data) {
        ServerPlayNetworking.send(player, removePacket(), data);
    }

    private void save(MinecraftServer server, boolean clean) {
        if (clean)
            this.fileManager.setLocked(true);

        for (T tardis : this.lookup.values()) {
            if (clean) {
                if (tardis == null)
                    continue;

                tardis.dispose();
            }

            this.fileManager.save(server, this, tardis);
        }

        if (!clean)
            return;
    }

    private void saveAndReset(MinecraftServer server) {
        this.save(server, true);
        this.reset();
    }


    /**
     * @return An initialized {@link RootComponent} without attachments.
     */
    protected T read(Gson gson, JsonObject json) {
        T tardis = gson.fromJson(json, getRootComponentType());
        RootComponent.init(tardis, SyncComponent.InitContext.deserialize());

        return tardis;
    }

    private void send(ServerPlayerEntity player, PacketByteBuf data) {
        ServerPlayNetworking.send(player, sendPacket(), data);
    }

    private void sendComponents(ServerPlayerEntity player, PacketByteBuf data) {
        ServerPlayNetworking.send(player, sendComponentPacket(), data);
    }

    private void writeSend(T tardis, PacketByteBuf buf) {
        buf.writeUuid(tardis.getUuid());
        buf.writeString(this.networkGson.toJson(tardis, this.getRootComponentType()));
    }

    private void writeComponent(SyncComponent component, PacketByteBuf buf) {
        String rawId = this.getRegistry().get(component);

        buf.writeString(rawId);
        buf.writeString(this.networkGson.toJson(component));
    }

    private PacketByteBuf prepareSend(T tardis) {
        PacketByteBuf data = PacketByteBufs.create();
        this.writeSend(tardis, data);

        return data;
    }

    private PacketByteBuf prepareSendDelta(T tardis) {
        PacketByteBuf data = PacketByteBufs.create();

        data.writeUuid(tardis.getUuid());
        data.writeShort(tardis.data().getDeltaSize());

        return data;
    }

    protected void sendBulk(ServerPlayerEntity player, Set<T> set) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeInt(set.size());

        for (T tardis : set) {
            if (isInvalid(tardis))
                continue;

            this.writeSend(tardis, data);
        }

        ServerPlayNetworking.send(player, sendBulkPacket(), data);
    }

    protected void sendAll(ServerPlayerEntity player, Set<T> set) {
        for (T tardis : set) {
            if (isInvalid(tardis))
                continue;

            // TardisEvents.SEND_TARDIS.invoker().send(tardis, player);
            this.send(player, this.prepareSend(tardis));
        }
    }

    protected void sendAll(Set<T> set) {
        for (T tardis : set) {
            if (isInvalid(tardis))
                continue;

            PacketByteBuf buf = this.prepareSend(tardis);

            this.getSubscribedPlayers(tardis).forEach(
                    watching -> {
                        this.send(watching, buf);
                    }
            );
        }
    }
    public void markComponentDirty(SyncComponent component) {
        if (this.fileManager.isLocked())
            return;

        if (!(component.parent() instanceof RootComponent))
            return;

        @SuppressWarnings("unchecked")
        T tardis = (T) component.parent();

        if (isInvalid(tardis))
            return;

        tardis.data().markDirty(component);
        this.delta.add(tardis);
    }

    public void markPropertyDirty(T tardis, Value<?> value) {
        this.markComponentDirty(value.getHolder());
    }

    public boolean add(T val) {
        if (this.lookup.containsValue(val)) {
            return false;
        }

        this.lookup.put(val);

        Initializable.init(val, SyncComponent.InitContext.createdAt(null));

        return true;
    }

    public abstract Set<ServerPlayerEntity> getSubscribedPlayers(T root);

    protected boolean isInvalid(T tardis) {
        return tardis == null || tardis.data().isRemoved();
    }

    protected abstract Class<T> getRootComponentType();
}
