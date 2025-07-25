package dev.amble.lib;

import dev.drtheo.multidim.MultiDimMod;
import dev.drtheo.scheduler.SchedulerMod;
import mc.duzo.animation.DuzoAnimationMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;

import dev.amble.lib.api.AmbleKitInitializer;
import dev.amble.lib.register.AmbleRegistries;
import dev.amble.lib.util.ServerLifecycleHooks;

public class AmbleKit implements ModInitializer {
    public static final String MOD_ID = "amblekit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        new DuzoAnimationMod().onInitialize();
        new SchedulerMod().onInitialize();
        new MultiDimMod().onInitialize();

        AmbleRegistries.getInstance();
        ServerLifecycleHooks.init();

        FabricLoader.getInstance().invokeEntrypoints("amblekit-main", AmbleKitInitializer.class,
                AmbleKitInitializer::onInitialize);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}