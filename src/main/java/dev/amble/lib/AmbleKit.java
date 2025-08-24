package dev.amble.lib;

import dev.drtheo.multidim.MultiDimMod;
import dev.drtheo.scheduler.SchedulerMod;
import mc.duzo.animation.DuzoAnimationMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;
import net.minecraftforge.fml.common.Mod;

import dev.amble.lib.register.AmbleRegistries;
import dev.amble.lib.util.ServerLifecycleHooks;

@Mod(AmbleKit.MOD_ID)
public class AmbleKit {
    public static final String MOD_ID = "amblekit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void onInitialize() {
        AmbleRegistries.getInstance();
        ServerLifecycleHooks.init();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}