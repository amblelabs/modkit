package mc.duzo.animation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;

import mc.duzo.animation.generic.AnimationInfo;
import mc.duzo.animation.util.AnimationUtil;

@Mixin(Perspective.class)
public class PerspectiveMixin {
    @Inject(method = "isFirstPerson", at = @At("HEAD"), cancellable = true)
    private void animation$isFirstPerson(CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return;

        AnimationInfo info = AnimationUtil.getInfo(player);

        if (info == null) return;
        if (info.perspective() == null) return;

        cir.setReturnValue(info.perspective().isFirstPerson());
    }
    @Inject(method = "isFrontView", at = @At("HEAD"), cancellable = true)
    private void animation$isFrontView(CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return;

        AnimationInfo info = AnimationUtil.getInfo(player);

        if (info == null) return;
        if (info.perspective() == null) return;

        cir.setReturnValue(info.perspective().isFrontView());
    }
}