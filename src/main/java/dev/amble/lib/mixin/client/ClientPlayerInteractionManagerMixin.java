package dev.amble.lib.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import dev.amble.lib.api.ICantBreak;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "breakBlock", at = @At(value = "HEAD"), cancellable = true)
    public void ait$breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        World world = this.client.world;
        if (world == null)
            return;

        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof ICantBreak cantBreak) {
            cantBreak.onTryBreak(world, pos, world.getBlockState(pos), this.client.player);
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
