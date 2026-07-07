package dev.amble.plushies;

import dev.amble.lib.animation.HasBedrockModel;
import dev.amble.lib.container.impl.BlockEntityContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;

public class PlushieBlockEntities implements BlockEntityContainer {

    public static final BlockEntityType<MarketablePlushieBlockEntity> MARKETABLE_PLUSHIE_BLOCK_ENTITY_TYPE =
            FabricBlockEntityTypeBuilder.create(MarketablePlushieBlockEntity::new,
                PlushieBlocks.LOQOR_MARKETABLE_PLUSHIE,
                PlushieBlocks.THEO_MARKETABLE_PLUSHIE,
                PlushieBlocks.ADDIE_MARKETABLE_PLUSHIE,
                PlushieBlocks.SATURN_MARKETABLE_PLUSHIE,
                PlushieBlocks.AVERY_MARKETABLE_PLUSHIE,
                PlushieBlocks.WANZZ_MARKETABLE_PLUSHIE,
                PlushieBlocks.EMBER_MARKETABLE_PLUSHIE,
                PlushieBlocks.LAKE_MARKETABLE_PLUSHIE,
                PlushieBlocks.CLASSIC_MARKETABLE_PLUSHIE,
                PlushieBlocks.BEN_MARKETABLE_PLUSHIE,
                PlushieBlocks.NYX_MARKETABLE_PLUSHIE,
                PlushieBlocks.MAX_MARKETABLE_PLUSHIE,
                PlushieBlocks.RHYNO_MARKETABLE_PLUSHIE,
                PlushieBlocks.MONKE_MARKETABLE_PLUSHIE,
                PlushieBlocks.KKING_MARKETABLE_PLUSHIE,
                PlushieBlocks.COSMIC_MARKETABLE_PLUSHIE,
                PlushieBlocks.DIAN_MARKETABLE_PLUSHIE,
                PlushieBlocks.TREE_MARKETABLE_PLUSHIE,
                PlushieBlocks.ECHO_MARKETABLE_PLUSHIE
            ).build();
}
