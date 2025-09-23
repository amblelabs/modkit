package dev.amble.lib.mixin.networking;

import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.WorldChunk;

import dev.amble.lib.api.KitEvents;

@Mixin(value = ThreadedAnvilChunkStorage.class, priority = 1001)
public abstract class ThreadedAnvilChunkStorageMixin {

    @Inject(method = "sendChunkDataPackets", at = @At("TAIL"))
    public void sendChunkDataPackets(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk, CallbackInfo ci) {
        KitEvents.SYNC_ROOT.invoker().sync(player, chunk);
    }
}
