package mc.duzo.animation.mixin;

import mc.duzo.animation.generic.VisibilityList;
import mc.duzo.animation.generic.VisiblePart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

import mc.duzo.animation.generic.AnimationInfo;
import mc.duzo.animation.util.AnimationUtil;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "setModelPose", at = @At("TAIL"))
    private void animation$playerRender(AbstractClientPlayerEntity player, CallbackInfo ci) {
        boolean current = this.getModel().body.visible;
        if (!(current)) return;

        MinecraftClient client = MinecraftClient.getInstance();

        AnimationInfo info = AnimationUtil.getInfo(player);
        if (info == null) return;

        VisibilityList type = info.render();

        type.apply(this.getModel(), player);
    }
}