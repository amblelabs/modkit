package mc.duzo.animation.player.holder;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import mc.duzo.animation.generic.AnimationHolder;
import mc.duzo.animation.generic.AnimationInfo;
import mc.duzo.animation.player.PlayerAnimationHelper;

public class PlayerAnimationHolder extends AnimationHolder {
    public PlayerAnimationHolder(Identifier id, Animation anim, AnimationInfo info) {
        super(id, anim, info);
    }

    public PlayerAnimationHolder(Identifier id, Animation anim) {
        super(id, anim);
    }

    @Override
    public void update(EntityModel<?> model, float progress, LivingEntity player) {
        PlayerAnimationHelper.updateAnimation(this.state, this.animation, progress, 1.0f, (PlayerEntityModel<?>) model);

        super.update(model, progress, player);
    }

}
