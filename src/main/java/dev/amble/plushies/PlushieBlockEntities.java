package dev.amble.plushies;

import dev.amble.lib.animation.HasBedrockModel;
import dev.amble.lib.container.impl.BlockEntityContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;

public class PlushieBlockEntities implements BlockEntityContainer {

    public static final BlockEntityType<MarketablePlushieBlockEntity> MARKETABLE_PLUSHIE_BLOCK_ENTITY_TYPE =
            FabricBlockEntityTypeBuilder.create(MarketablePlushieBlockEntity::new,
                    PlushieBlocks.getAllMarketablePlushies()
            ).build();
}
