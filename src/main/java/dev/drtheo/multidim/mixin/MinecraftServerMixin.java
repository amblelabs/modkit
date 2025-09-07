package dev.drtheo.multidim.mixin;

import com.google.common.collect.Maps;
import dev.drtheo.multidim.MultiDimMod;
import dev.drtheo.multidim.api.MultiDimServer;
import dev.drtheo.multidim.event.ServerCrashEvent;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MultiDimServer {

    @Shadow @Final
    protected LevelStorage.Session session;

    @Shadow
    @Final
    @Mutable
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Override
    public void multidim$addWorld(ServerWorld world) {
        // use read-copy-update to avoid concurrency issues
        // from immersive portals
        LinkedHashMap<RegistryKey<World>, ServerWorld> newMap =
                Maps.newLinkedHashMap();

        Map<RegistryKey<World>, ServerWorld> oldMap = this.worlds;

        newMap.putAll(oldMap);
        newMap.put(world.getRegistryKey(), world);

        this.worlds = newMap;
    }

    @Override
    public boolean multidim$hasWorld(RegistryKey<World> key) {
        return this.worlds.containsKey(key);
    }

    @Override
    public ServerWorld multidim$removeWorld(RegistryKey<World> key) {
        // use read-copy-update to avoid concurrency issues
        // from immersive portals
        LinkedHashMap<RegistryKey<World>, ServerWorld> newMap =
                Maps.newLinkedHashMap();

        Map<RegistryKey<World>, ServerWorld> oldMap = this.worlds;

        for (Map.Entry<RegistryKey<World>, ServerWorld> entry : oldMap.entrySet()) {
            if (entry.getKey() != key) {
                newMap.put(entry.getKey(), entry.getValue());
            }
        }

        this.worlds = newMap;

        return oldMap.get(key);
    }

    @Override
    public LevelStorage.Session multidim$getSession() {
        return this.session;
    }

    @Inject(method = "setCrashReport", at = @At("TAIL"))
    private void ait$setCrashReport(CrashReport report, CallbackInfo info) {
        MultiDimMod.LOGGER.error("Crash Detected - nice one m8");
        ServerCrashEvent.EVENT.invoker().onServerCrash((MinecraftServer) (Object) this, report);
    }
}
