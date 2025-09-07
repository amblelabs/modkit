package dev.amble.lib.mixin.fix;

import dev.drtheo.multidim.MultiDim;
import dev.drtheo.multidim.impl.SimpleWorldProgressListener;
import dev.drtheo.multidim.util.MultiDimUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.ProgressListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiDim.class)
public class WorldEventsFix {

    @Shadow(remap = false)
    @Final protected MinecraftServer server;

    @Redirect(method = "load(Ldev/drtheo/multidim/api/MultiDimServerWorld;)V", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/lifecycle/v1/ServerWorldEvents$Load;onWorldLoad(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/world/ServerWorld;)V"), remap = false)
    public void load(ServerWorldEvents.Load instance, MinecraftServer minecraftServer, ServerWorld serverLevel) {
        MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(serverLevel));
    }

    @Redirect(method = "unload", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;save(Lnet/minecraft/util/ProgressListener;ZZ)V"), remap = false)
    public void unload(ServerWorld instance, ProgressListener progressListener, boolean b1, boolean b2) {
        instance.save(new SimpleWorldProgressListener(() -> {
            MinecraftForge.EVENT_BUS.post(new LevelEvent.Unload(instance));

            Identifier key = instance.getDimensionKey().getValue();
            MultiDimUtil.getMutableDimensionsRegistry(this.server).multidim$remove(key);
        }), b1, b2);
    }
}
