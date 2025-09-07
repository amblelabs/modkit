package mc.duzo.animation.util;

import net.minecraft.client.network.AbstractClientPlayerEntity;

import mc.duzo.animation.api.AnimationEvents;
import mc.duzo.animation.generic.AnimationHolder;
import mc.duzo.animation.generic.AnimationInfo;
import mc.duzo.animation.generic.AnimationTracker;
import mc.duzo.animation.registry.client.TrackerRegistry;


public class AnimationUtil {
    public static AnimationInfo getInfo(AbstractClientPlayerEntity player) {
        for (AnimationTracker tracker : TrackerRegistry.REGISTRY) {
            AnimationHolder holder = tracker.get(player);
            if (holder == null) continue;

            return holder.getInfo();
        }

        AnimationEvents.Result<AnimationInfo> result = AnimationEvents.FIND_ANIMATION_INFO.invoker().getAnimationInfo(player);
        if (result.result().isEmpty()) return null;

        return result.result().get();
    }

    public static boolean isRunningAnimations(AbstractClientPlayerEntity player) {
        AnimationInfo info = getInfo(player);
        return info != null && info.transform() != null;
    }
}
