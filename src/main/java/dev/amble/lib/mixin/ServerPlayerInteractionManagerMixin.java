package dev.amble.lib.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import dev.amble.lib.api.ICantBreak;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    protected ServerWorld world;

    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At(value = "HEAD"), cancellable = true)
    public void ait$tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Block block = this.world.getBlockState(pos).getBlock();
        if (block instanceof ICantBreak cantBreak) {
            cantBreak.onTryBreak(this.world, pos, this.world.getBlockState(pos), this.player);
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
