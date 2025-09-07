package mc.duzo.animation.player;

import java.util.Optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.AnimationState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import mc.duzo.animation.player.holder.PlayerAnimationHolder;
import mc.duzo.animation.registry.AnimationRegistry;

/**
 * From {@link net.minecraft.client.render.entity.animation.AnimationHelper},
 * Edited for animating PlayerModel
 *
 * @author duzo
 */
@Environment(value= EnvType.CLIENT)
public class PlayerAnimationHelper {
    public static void animate(PlayerEntityModel<?> model, Animation animation, long runningTime, float scale, Vector3f tempVec) {
        float f = getRunningSeconds(animation, runningTime);
        animation.boneAnimations().forEach((key, list) -> {
            // TODO - fix "player" / "root" part not animating properly
            Optional<ModelPart> optional = getChild(model, key);
            optional.ifPresent(ModelPart::resetTransform);
            optional.ifPresent(part -> list.forEach(transformation -> {
                Keyframe[] keyframes = transformation.keyframes();
                int i = Math.max(0, MathHelper.binarySearch(0, keyframes.length, index -> f <= keyframes[index].timestamp()) - 1);
                int j = Math.min(keyframes.length - 1, i + 1);
                Keyframe keyframe = keyframes[i];
                Keyframe keyframe2 = keyframes[j];
                float h = f - keyframe.timestamp();
                float k = j != i ? MathHelper.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
                keyframe2.interpolation().apply(tempVec, k, keyframes, i, j, scale);
                transformation.target().apply(part, tempVec);
            }));
        });

        correctSleevies(model);
    }

    private static void correctSleevies(PlayerEntityModel<?> model) {
        model.hat.copyTransform(model.head);
        model.leftSleeve.copyTransform(model.leftArm);
        model.rightSleeve.copyTransform(model.rightArm);
        model.leftPants.copyTransform(model.leftLeg);
        model.rightPants.copyTransform(model.rightLeg);
        model.jacket.copyTransform(model.body);
    }

    public static void updateAnimation(AnimationState animationState, Animation animation, float animationProgress, float speedMultiplier, PlayerEntityModel<?> model) {
        animationState.update(animationProgress, speedMultiplier);
        animationState.run(state -> animate(model, animation, state.getTimeRunning(), 1.0f, new Vector3f()));
    }
    public static void updateAnimation(AnimationState animationState, Animation animation, float animationProgress, PlayerEntityModel<?> model) {
        updateAnimation(animationState, animation, animationProgress, 1.0f, model);
    }

    public static float getRunningSeconds(Animation animation, long runningTime) {
        float f = (float)runningTime / 1000.0f;
        return animation.looping() ? f % animation.lengthInSeconds() : f;
    }

    private static Optional<ModelPart> getChild(PlayerEntityModel<?> model, String name) {
        return ((PlayerModelHook) model).animation$getChild(name);
    }

    public static Vector3f getModelPartPivot(PlayerEntityModel<?> model, String name) {
        Optional<ModelPart> optional = getChild(model, name);
        if(optional.isEmpty()) return new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f pivot = new Vector3f(optional.get().pivotX, optional.get().pivotY, optional.get().pivotZ);
        return pivot;
    }

    public static Vector3f getModelPartRotation(PlayerEntityModel<?> model, String name) {
        Optional<ModelPart> optional = getChild(model, name);
        if(optional.isEmpty()) return new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f rotation = new Vector3f(optional.get().pivotX, optional.get().pivotY, optional.get().pivotZ);
        return rotation;
    }

    public static void runAnimations(AbstractClientPlayerEntity livingEntity, PlayerEntityModel<?> model, float progress) {
        PlayerAnimationHolder anim = PlayerAnimationTracker.getInstance().get(livingEntity);

        if (anim != null) {
            anim.update(model, progress, livingEntity);
            return;
        }
    }

    public static void startAnimations(AbstractClientPlayerEntity entity) {

    }
    public static boolean isRunningAnimations(AbstractClientPlayerEntity livingEntity) {
        PlayerAnimationHolder anim = PlayerAnimationTracker.getInstance().get(livingEntity);
        return (anim != null && !anim.isFinished(livingEntity));
    }

    public static void playAnimation(AbstractClientPlayerEntity player, Identifier animation) {
        playAnimation(player, (PlayerAnimationHolder) AnimationRegistry.instance().get(animation).get());
    }
    public static void playAnimation(AbstractClientPlayerEntity player, PlayerAnimationHolder holder) {
        stopAnimation(player);
        PlayerAnimationTracker.getInstance().add(player.getUuid(), holder);
    }
    public static void stopAnimation(AbstractClientPlayerEntity player) {
        PlayerAnimationTracker.getInstance().clear(player.getUuid());
    }
    public static final Transformation.Interpolation STEP = (dest, delta, keyframes, start, end, scale) -> {
        Vector3f vector3f = keyframes[start].target();
        Vector3f vector3f2 = keyframes[end].target();
        return vector3f.lerp(vector3f2, 0, dest).mul(scale);
    };
}