package mc.duzo.animation.registry;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import mc.duzo.animation.DuzoAnimationMod;
import mc.duzo.animation.generic.AnimationHolder;

public class AnimationRegistry<H extends AnimationHolder, T extends Supplier<H>> {
    private static AnimationRegistry INSTANCE;

    public final SimpleRegistry<T> REGISTRY = FabricRegistryBuilder.createSimple(RegistryKey.<T>ofRegistry(new Identifier(DuzoAnimationMod.MOD_ID, "animations"))).buildAndRegister();

    public T register(T entry) {
        return Registry.register(REGISTRY, entry.get().id(), entry);
    }
    public Supplier<H> get(Identifier id) { return REGISTRY.get(id); }

    public static void init() {
        instance();
    }

    public static AnimationRegistry instance() {
        if (INSTANCE == null) {
            INSTANCE = new AnimationRegistry();
        }

        return INSTANCE;
    }
}
