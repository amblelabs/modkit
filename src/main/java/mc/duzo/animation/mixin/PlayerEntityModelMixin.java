package mc.duzo.animation.mixin;

import java.util.List;
import java.util.Optional;

import mc.duzo.animation.generic.AnimationInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

import mc.duzo.animation.player.PlayerAnimationHelper;
import mc.duzo.animation.player.PlayerModelHook;
import mc.duzo.animation.util.AnimationUtil;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity>
        extends BipedEntityModel<T> implements PlayerModelHook {

    @Shadow
    @Final
    private List<ModelPart> parts;

    @Unique ModelPart root;

    public PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void animation$init(ModelPart root, boolean thinArms, CallbackInfo ci) {
        this.root = root;
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void animation$setAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (!(livingEntity instanceof AbstractClientPlayerEntity player)) return;

        PlayerAnimationHelper.startAnimations(player);

        if (!AnimationUtil.isRunningAnimations(player)) return;

        if (AnimationUtil.getInfo(player).transform() == AnimationInfo.Transform.ALL) {
            this.animation$getPart().resetTransform();
            this.parts.forEach(ModelPart::resetTransform);
        }

        PlayerEntityModel<?> model = (PlayerEntityModel<?>) (Object) this;

        PlayerAnimationHelper.runAnimations(player, model, h);
    }

    @Override
    public Optional<ModelPart> animation$getChild(String name) {
        if (name.equals("root") || name.equalsIgnoreCase("player")) {
            return Optional.of(this.animation$getPart());
        }
        return this.animation$getPart().traverse().filter(part -> part.hasChild(name)).findFirst().map(part -> part.getChild(name));
    }

    @Override
    public ModelPart animation$getPart() {
        return this.root;
    }
}