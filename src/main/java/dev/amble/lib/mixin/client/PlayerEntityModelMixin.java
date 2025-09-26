package dev.amble.lib.mixin.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.client.AnimatedEntityModel;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity>
        extends BipedEntityModel<T> implements AnimatedEntityModel {

    @Unique
    ModelPart root;

    public PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void animation$init(ModelPart root, boolean thinArms, CallbackInfo ci) {
        this.root = root;
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void animation$setAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (!(livingEntity instanceof AnimatedEntity player)) return;

		PlayerEntityModel model = (PlayerEntityModel)(Object)this;
	    model.hat.copyTransform(model.head);
	    model.leftPants.copyTransform(model.leftLeg);
	    model.rightPants.copyTransform(model.rightLeg);
	    model.leftSleeve.copyTransform(model.leftArm);
	    model.rightSleeve.copyTransform(model.rightArm);
	    model.jacket.copyTransform(model.body);

	    if (!BedrockAnimation.isRenderingPlayer || livingEntity != MinecraftClient.getInstance().cameraEntity) return;

	    hat.visible = false;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}