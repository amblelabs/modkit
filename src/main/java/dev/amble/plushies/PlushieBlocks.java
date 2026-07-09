package dev.amble.plushies;

import dev.amble.lib.block.ABlockSettings;
import dev.amble.lib.container.impl.BlockContainer;
import dev.amble.lib.item.AItemSettings;
import dev.amble.lib.mixin.AbstractBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlushieBlocks extends BlockContainer {

    public static final List<String> DEVS = List.of(
            "loqor", "theo", "addie", "saturn",
            "avery", "wanzz", "ember", "max",
            "lake", "classic", "pursephone", "ben",
            "nyx", "rhyno", "monke", "kking",
            "cosmic", "dian", "tree", "echo",
            "lucien", "maggie"
    );

    public static final ArrayList<Block> MARKETABLE_PLUSHIES = new ArrayList<>();

    public static Block[] getAllMarketablePlushies() {
        return MARKETABLE_PLUSHIES.toArray(new Block[0]);
    }

    @Override
    public Item.Settings createBlockItemSettings(Block block) {
        return new AItemSettings().group(PlushieItemGroups.PLUSHIES);
    }

    public static void registerAll(String namespace) {
        PlushieBlocks self = new PlushieBlocks();
        self.start(DEVS.size());

        for (String name : DEVS) {
            ABlockSettings settings = new ABlockSettings();
            Block block = new MarketablePlushieBlock(settings, name);
            Identifier id = new Identifier(namespace, name + "_marketable_plushie");

            Registry.register(Registries.BLOCK, id, block);

            Item item = self.createBlockItem(block, settings.itemSettings());

            Registry.register(Registries.ITEM, id, item);
            self.items.add(item);

            MARKETABLE_PLUSHIES.add(block);
        }

        self.finish();
    }
}