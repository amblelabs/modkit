package dev.amble.plushies;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.container.RegistryContainer;
import dev.amble.plushies.api.PlushiesInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Plushies implements ModInitializer {

    @Override
    public void onInitialize() {
        RegistryContainer.register(PlushieBlocks.class, AmbleKit.MOD_ID);
        RegistryContainer.register(PlushieBlockEntities.class, AmbleKit.MOD_ID);
        RegistryContainer.register(PlushieItemGroups.class, AmbleKit.MOD_ID);
        RegistryContainer.register(PlushieSounds.class, AmbleKit.MOD_ID);

        FabricLoader.getInstance().invokeEntrypoints("plushies-main", PlushiesInitializer.class,
                PlushiesInitializer::onInitialize);
    }
}
