package mc.duzo.animation.generic;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import mc.duzo.animation.player.PlayerAnimationHelper;
import mc.duzo.animation.registry.Identifiable;

public abstract class AnimationHolder implements Identifiable {
    private final Identifier id;
    protected final AnimationState state;
    protected final Animation animation;
    protected final AnimationInfo info;

    protected AnimationHolder(Identifier id, Animation anim, AnimationInfo info) {
        this.id = id;
        this.state = new AnimationState();
        this.animation = anim;
        this.info = info;
    }
    protected AnimationHolder(Identifier id, Animation anim) {
        this(id, anim, new AnimationInfo(VisibilityList.all(), AnimationInfo.Perspective.THIRD_PERSON_FRONT, AnimationInfo.Movement.DISABLE, AnimationInfo.Transform.ALL));
    }

    @Override
    public Identifier id() {
        return this.id;
    }

    public void update(EntityModel<?> model, float progress, LivingEntity player) {
        // overwritten update method goes here

        if (this.isFinished(player)) {
            this.state.stop();
            this.onFinished(player);
            return;
        }

        if (!this.state.isRunning()) {
            this.onStart(player);
        }

        this.state.startIfNotRunning(player.age);
    }

    public boolean isFinished(LivingEntity entity) {
        if (this.animation.looping()) return false; // Looping animations should extend this class so they properly finish

        return this.getRunningSeconds() >= this.animation.lengthInSeconds();
    }

    protected void onFinished(LivingEntity player) {

    }
    protected void onStart(LivingEntity player) {

    }

    protected float getRunningSeconds() {
        return PlayerAnimationHelper.getRunningSeconds(this.animation, this.state.getTimeRunning());
    }
    public Animation getAnimation() {
        return animation;
    }

    public AnimationInfo getInfo() {
        return this.info;
    }
}
