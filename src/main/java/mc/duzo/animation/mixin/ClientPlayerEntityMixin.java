package mc.duzo.animation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import mc.duzo.animation.generic.AnimationInfo;
import mc.duzo.animation.util.AnimationUtil;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void animation$tickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return;

        AnimationInfo info = AnimationUtil.getInfo(player);

        if (info == null || info.movement() == AnimationInfo.Movement.ALLOW) return;

        ci.cancel();
    }

}
