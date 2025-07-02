package dev.amble.lib.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.chunk.WorldChunk;

public class KitEvents {
    public static final Event<PreDatapackLoad> PRE_DATAPACK_LOAD = EventFactory.createArrayBacked(PreDatapackLoad.class, callbacks -> () -> {
        for (PreDatapackLoad callback : callbacks) {
            callback.load();
        }
    });

    public static final Event<SyncRoot> SYNC_ROOT = EventFactory.createArrayBacked(SyncRoot.class,
            callbacks -> (player, chunk) -> {
                for (SyncRoot callback : callbacks) {
                    callback.sync(player, chunk);
                }
            });

    /**
     * Called when just before datapacks are loaded
     */
    @FunctionalInterface
    public interface PreDatapackLoad {
        void load();
    }

    @FunctionalInterface
    public interface SyncRoot {
        void sync(ServerPlayerEntity player, WorldChunk chunk);
    }
}
