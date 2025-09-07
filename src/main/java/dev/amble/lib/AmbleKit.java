package dev.amble.lib;

import dev.amble.lib.client.AmbleKitClient;
import dev.drtheo.scheduler.SchedulerMod;
import mc.duzo.animation.DuzoAnimationMod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    public AmbleKit() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    public void onInitialize() {
        new SchedulerMod().onInitialize();
        new DuzoAnimationMod().onInitialize();

        AmbleRegistries.getInstance();
        ServerLifecycleHooks.init();
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        Runnable e = () -> {
            new AmbleKitClient().onInitializeClient();
        };

        e.run();
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        this.onInitialize();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}