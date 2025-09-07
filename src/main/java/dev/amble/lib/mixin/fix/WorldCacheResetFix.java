package dev.amble.lib.mixin.fix;

import dev.drtheo.multidim.api.MultiDimServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftServer.class, priority = 1001)
public abstract class WorldCacheResetFix implements MultiDimServer {

    @Shadow(remap = false) private ServerWorld[] worldArray;

    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
    @Inject(method = "multidim$addWorld", at = @At("TAIL"), remap = false)
    public void addWorld(ServerWorld level, CallbackInfo ci) {
        this.worldArray = null; // reset world array cache
    }
}
