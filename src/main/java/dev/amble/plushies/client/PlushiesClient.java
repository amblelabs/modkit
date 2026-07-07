package dev.amble.plushies.client;

import dev.amble.plushies.PlushieBlockEntities;
import dev.amble.plushies.api.client.PlushiesClientInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class PlushiesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().invokeEntrypoints("plushies-client", PlushiesClientInitializer.class,
                PlushiesClientInitializer::onInitialize);
        registerBlockEntityRenderers();
    }

    private static void registerBlockEntityRenderers() {
        BlockEntityRendererFactories.register(PlushieBlockEntities.MARKETABLE_PLUSHIE_BLOCK_ENTITY_TYPE, MarketablePlushieRenderer::new);
    }
}
