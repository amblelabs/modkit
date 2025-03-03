package dev.amble.lib.api.sync.manager.client;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.api.sync.handler.SyncComponent;
import dev.amble.lib.api.sync.manager.SyncManager;

public abstract class ClientSyncManager<T extends RootComponent & ClientRootComponent> extends SyncManager<T, MinecraftClient> {
    private final Multimap<UUID, Consumer<T>> subscribers = ArrayListMultimap.create();

    protected ClientSyncManager() {
        super();

        ClientPlayNetworking.registerGlobalReceiver(sendPacket(), (client, handler, buf, responseSender) -> this.syncTardis(buf));

        ClientPlayNetworking.registerGlobalReceiver(sendBulkPacket(),
                (client, handler, buf, responseSender) -> this.syncBulk(buf));

        ClientPlayNetworking.registerGlobalReceiver(removePacket(), (client, handler, buf, responseSender) -> this.remove(buf));

        ClientPlayNetworking.registerGlobalReceiver(sendComponentPacket(), (client, handler, buf, responseSender) -> this.syncDelta(buf));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null)
                return;

            for (T tardis : this.lookup.values()) {
                tardis.tick(client);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.reset());
        ClientLoginConnectionEvents.DISCONNECT.register((client, reason) -> this.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> this.reset());
    }

    private void remove(PacketByteBuf buf) {
        this.lookup.remove(buf.readUuid());
    }

    @Override
    public void load(MinecraftClient client, UUID uuid, @Nullable Consumer<T> consumer) {
        if (client.player == null)
            return;

        if (uuid == null)
            return;

        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(uuid);

        if (consumer != null)
            this.subscribers.put(uuid, consumer);

        // MinecraftClient.getInstance().executeTask(() -> ClientPlayNetworking.send(ASK, data));
    }

    @Override
    @Deprecated
    public @Nullable T demand(MinecraftClient client, UUID uuid) {
        T result = this.lookup.get(uuid);

        if (result == null)
            this.load(client, uuid, null);

        return result;
    }

    @Deprecated
    public @Nullable T demand(UUID uuid) {
        return this.demand(MinecraftClient.getInstance(), uuid);
    }

    public void get(UUID uuid, Consumer<T> consumer) {
        this.get(MinecraftClient.getInstance(), uuid, consumer);
    }

    @Override
    public void reset() {
        this.subscribers.clear();

        this.forEach(T::dispose);
        super.reset();
    }

    private void syncDelta(PacketByteBuf buf) {
        UUID id = buf.readUuid();
        int count = buf.readShort();

        T tardis = this.demand(id);

        if (tardis == null)
            return; // wait 'till the server sends a full update

        for (int i = 0; i < count; i++) {
            this.syncComponent(tardis, buf);
        }
    }

    private void syncTardis(UUID uuid, String json) {
        try {
            T tardis = this.networkGson.fromJson(json, getRootComponentType());
            RootComponent.init(tardis, SyncComponent.InitContext.deserialize());

            // tardis.travel(); // get a random element. if its null it will complain

            synchronized (this) {
                T old = this.lookup.put(tardis);

                if (old != null)
                    old.age();

                for (Consumer<T> consumer : this.subscribers.removeAll(uuid)) {
                    consumer.accept(tardis);
                }
            }
        } catch (Throwable t) {
            AmbleKit.LOGGER.error("Received malformed JSON file {}", json);
            AmbleKit.LOGGER.error("Failed to deserialize {}/{} data: ", modId(), name(), t);
        }
    }

    private void syncTardis(PacketByteBuf buf) {
        this.syncTardis(buf.readUuid(), buf.readString());
    }

    private void syncBulk(PacketByteBuf buf) {
        int count = buf.readInt();

        for (int i = 0; i < count; i++) {
            this.syncTardis(buf);
        }
    }

    private void syncComponent(T tardis, PacketByteBuf buf) {
        String rawId = buf.readString();

        SyncComponent.IdLike id = this.getRegistry().get(rawId);
        SyncComponent component = this.networkGson.fromJson(buf.readString(), id.clazz());

        id.set(tardis, component);
        SyncComponent.init(component, tardis, SyncComponent.InitContext.deserialize());
    }

    protected abstract Class<T> getRootComponentType();
}
