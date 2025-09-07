package mc.duzo.animation.player;

import net.minecraft.util.Identifier;

import mc.duzo.animation.DuzoAnimationMod;
import mc.duzo.animation.generic.AnimationTracker;
import mc.duzo.animation.player.holder.PlayerAnimationHolder;
import mc.duzo.animation.registry.client.TrackerRegistry;

public class PlayerAnimationTracker extends AnimationTracker<PlayerAnimationHolder> {

    public PlayerAnimationTracker() {
        super(new Identifier(DuzoAnimationMod.MOD_ID, "player"));
    }

    public static PlayerAnimationTracker getInstance() {
        return TrackerRegistry.PLAYER;
    }
}
