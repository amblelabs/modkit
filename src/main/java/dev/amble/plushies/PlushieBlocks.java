package dev.amble.plushies;

import dev.amble.lib.animation.HasBedrockModel;
import dev.amble.lib.block.ABlockSettings;
import dev.amble.lib.container.impl.BlockContainer;
import dev.amble.lib.item.AItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class PlushieBlocks extends BlockContainer {

    public static final Block LOQOR_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "loqor");
    public static final Block THEO_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "theo");
    public static final Block ADDIE_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "addie");
    public static final Block SATURN_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "saturn");
    public static final Block AVERY_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "avery");
    public static final Block WANZZ_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "wanzz");
    public static final Block EMBER_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "ember");
    public static final Block LAKE_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "lake");
    public static final Block CLASSIC_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "classic");
    public static final Block BEN_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "ben");
    public static final Block NYX_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "nyx");
    public static final Block RHYNO_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "rhyno");
    public static final Block MONKE_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "monke");
    public static final Block KKING_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "kking");
    public static final Block COSMIC_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "cosmic");
    public static final Block DIAN_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "dian");
    public static final Block TREE_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "tree");
    public static final Block ECHO_MARKETABLE_PLUSHIE = new MarketablePlushieBlock(new ABlockSettings(), "echo");

    @Override
    public Item.Settings createBlockItemSettings(Block block) {
        return new AItemSettings().group(PlushieItemGroups.PLUSHIES);
    }
}
