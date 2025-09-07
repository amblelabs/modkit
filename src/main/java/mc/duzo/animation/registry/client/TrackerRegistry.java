package mc.duzo.animation.registry.client;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import mc.duzo.animation.DuzoAnimationMod;
import mc.duzo.animation.generic.AnimationTracker;
import mc.duzo.animation.player.PlayerAnimationTracker;

public class TrackerRegistry {
    public static final SimpleRegistry<AnimationTracker> REGISTRY = FabricRegistryBuilder.createSimple(RegistryKey.<AnimationTracker>ofRegistry(new Identifier(DuzoAnimationMod.MOD_ID, "tracker"))).buildAndRegister();

    public static <T extends AnimationTracker> T register(T entry) {
        return Registry.register(REGISTRY, entry.id(), entry);
    }

    public static final PlayerAnimationTracker PLAYER = (PlayerAnimationTracker) new PlayerAnimationTracker().register();

    public static void init() {

    }
}
