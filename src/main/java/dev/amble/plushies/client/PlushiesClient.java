package dev.amble.plushies.client;

import dev.amble.plushies.PlushieBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class PlushiesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(PlushieBlockEntities.MARKETABLE_PLUSHIE_BLOCK_ENTITY_TYPE, MarketablePlushieRenderer::new);
    }
}
