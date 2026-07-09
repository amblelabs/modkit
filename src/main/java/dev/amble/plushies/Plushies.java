package dev.amble.plushies;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.container.RegistryContainer;
import net.fabricmc.api.ModInitializer;

public class Plushies implements ModInitializer {

    @Override
    public void onInitialize() {
        PlushieBlocks.registerAll(AmbleKit.MOD_ID);
        RegistryContainer.register(PlushieBlockEntities.class, AmbleKit.MOD_ID);
        RegistryContainer.register(PlushieItemGroups.class, AmbleKit.MOD_ID);
        RegistryContainer.register(PlushieSounds.class, AmbleKit.MOD_ID);
    }
}
