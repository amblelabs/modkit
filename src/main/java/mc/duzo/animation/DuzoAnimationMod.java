package mc.duzo.animation;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import mc.duzo.animation.generic.AnimationHolder;
import mc.duzo.animation.generic.AnimationTracker;
import mc.duzo.animation.network.Network;
import mc.duzo.animation.network.PlayAnimationS2CPacket;
import mc.duzo.animation.registry.AnimationRegistry;

public class DuzoAnimationMod {
    public static final String MOD_ID = "animation";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void onInitialize() {
        LOGGER.info("thank you for using duzos animator!");

        AnimationRegistry.init();
    }

    // todo - move to better place
    public static void play(ServerPlayerEntity target, AnimationTracker tracker, AnimationHolder animation) {
        Network.toTracking(new PlayAnimationS2CPacket(target, tracker, animation), target);
    }

    /**
     * plays an animation to a target and updates all players near to the target
     * @param target the player to animate
     * @param tracker the tracker this animation should be performed on
     * @param animation identifier of the registered animation
     */
    public static void play(ServerPlayerEntity target, AnimationTracker tracker, Identifier animation) {
        Network.toTracking(new PlayAnimationS2CPacket(target.getUuid(), tracker.id(), animation), target);
    }
}