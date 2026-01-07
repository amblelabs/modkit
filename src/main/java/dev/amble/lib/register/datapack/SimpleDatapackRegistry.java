package dev.amble.lib.register.datapack;

import java.io.InputStream;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.api.Identifiable;
import dev.amble.lib.util.ServerLifecycleHooks;

public abstract class SimpleDatapackRegistry<T extends Identifiable> extends DatapackRegistry<T>
        implements
            SimpleSynchronousResourceReloadListener {

    private final Function<InputStream, T> deserializer;
    private final Codec<T> codec;
    protected final Identifier packet;
    private final Identifier name;
    private final boolean sync;

    public SimpleDatapackRegistry(Function<InputStream, T> deserializer, Codec<T> codec, Identifier packet,
            Identifier name, boolean sync) {
        this.deserializer = deserializer;
        this.codec = codec;
        this.packet = packet;
        this.name = name;
        this.sync = sync;
    }

    protected SimpleDatapackRegistry(Function<InputStream, T> deserializer, Codec<T> codec, String packet, String name,
            boolean sync, String modid) {
        this(deserializer, codec, new Identifier(modid, "sync_" + packet), new Identifier(modid, name),
                sync);
    }

    protected SimpleDatapackRegistry(Function<InputStream, T> deserializer, Codec<T> codec, String name, boolean sync, String modid) {
        this(deserializer, codec, name, name, sync, modid);
    }

    /**
     * @deprecated Use {@link #SimpleDatapackRegistry(Function, Codec, String, boolean, String)} instead and provide your own modid
     */
    @Deprecated(forRemoval = true, since = "1.0.11")
    protected SimpleDatapackRegistry(Function<InputStream, T> deserializer, Codec<T> codec, String name) {
        this(deserializer, codec, name, name, true, AmbleKit.MOD_ID);
    }

    public void onClientInit() {
        if (!this.sync)
            return;

        ClientPlayNetworking.registerGlobalReceiver(this.packet,
                (client, handler, buf, responseSender) -> this.readFromServer(buf));
    }

    /**
     * @implNote Currently not implemented as there's no dedicated server-side logic
     */
    public void onServerInit() {
    }

    public void onCommonInit() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this);

        if (!this.sync)
            return;

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> this.syncToClient(player));
    }

    @Override
    public void syncToEveryone() {
        if (!this.sync || ServerLifecycleHooks.get() == null)
            return;

        super.syncToEveryone();
    }

    @Override
    public void syncToClient(ServerPlayerEntity player) {
        if (!this.sync)
            return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(REGISTRY.size());

        for (T schema : REGISTRY.values()) {
            buf.encodeAsJson(this.codec, schema);
        }

        ServerPlayNetworking.send(player, this.packet, buf);
    }

    @Override
    public void readFromServer(PacketByteBuf buf) {
        if (!this.sync)
            return;

        // this.clearCache();
        this.defaults();
        int size = buf.readInt();

        for (int i = 0; i < size; i++) {
            this.register(buf.decodeAsJson(this.codec));
        }

        AmbleKit.LOGGER.info("Read {} {} from server", size, this.name);
    }

    protected abstract void defaults();

    protected T read(InputStream stream) {
        return this.deserializer.apply(stream);
    }

    @Override
    public Identifier getFabricId() {
        return SimpleDatapackRegistry.this.name;
    }

    @Override
    public void reload(ResourceManager manager) {
        // this.clearCache();
        this.defaults();

        for (Identifier id : manager
                .findResources(this.name.getPath(), filename -> filename.getPath().endsWith(".json")).keySet()) {
            try (InputStream stream = manager.getResource(id).get().getInputStream()) {
                T created = this.read(stream);

                if (created == null) {
                    stream.close();
                    continue;
                }

                this.register(created);
                AmbleKit.LOGGER.info("Loaded datapack {} {}", this.name, created.id().toString());
            } catch (Exception e) {
                AmbleKit.LOGGER.error("Error occurred while loading resource json {}", id.toString(), e);
            }
        }

        this.syncToEveryone();
    }
}
