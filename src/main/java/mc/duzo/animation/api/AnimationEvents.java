package mc.duzo.animation.api;

import java.util.Optional;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.client.network.AbstractClientPlayerEntity;

import mc.duzo.animation.generic.AnimationInfo;

public final class AnimationEvents {
    public static final Event<FindAnimationInfo> FIND_ANIMATION_INFO = EventFactory.createArrayBacked(FindAnimationInfo.class, callbacks -> player -> {
        for (FindAnimationInfo callback : callbacks) {
            Result<AnimationInfo> value = callback.getAnimationInfo(player);

            if (value.type() != Interaction.PASS || value.result().isPresent())
                return value;
        }
        return Result.success();
    });

    @FunctionalInterface
    public interface FindAnimationInfo {
        /**
         * called when the animation info is queried and NONE is found
         * @return the overwritten result if any
         */
        Result<AnimationInfo> getAnimationInfo(AbstractClientPlayerEntity player);
    }


    // from AIT
    public enum Interaction {
        SUCCESS, FAIL, PASS
    }

    public record Result<T>(Interaction type, Optional<T> t) {

        public static <T> Result<T> success() {
            return new Result<>(Interaction.SUCCESS);
        }

        public static <T> Result<T> fail() {
            return new Result<>(Interaction.FAIL);
        }

        public static <T> Result<T> pass() {
            return new Result<>(Interaction.PASS);
        }

        public Result(Interaction inter) {
            this(inter, Optional.empty());
        }

        public Result(T t) {
            this(Interaction.PASS, t);
        }

        public Result(Interaction inter, T t) {
            this(inter, Optional.ofNullable(t));
        }

        public Optional<T> result() {
            return t;
        }
    }
}
