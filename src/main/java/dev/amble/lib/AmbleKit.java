package dev.amble.lib;

import dev.amble.lib.command.SetSkinCommand;
import dev.amble.lib.skin.SkinTracker;
import dev.drtheo.multidim.MultiDimMod;
import dev.drtheo.scheduler.SchedulerMod;
import mc.duzo.animation.DuzoAnimationMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
    private static final boolean TESTS_ENABLED = true;

    @Override
    public void onInitialize() {
        AmbleRegistries.getInstance();
        ServerLifecycleHooks.init();
		SkinTracker.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
			SetSkinCommand.register(dispatcher);
		});

        FabricLoader.getInstance().invokeEntrypoints("amblekit-main", AmbleKitInitializer.class,
                AmbleKitInitializer::onInitialize);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
    public static boolean isTestingEnabled() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() && TESTS_ENABLED;
    }
}