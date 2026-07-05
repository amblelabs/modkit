package dev.amble.plushies;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.container.impl.ItemGroupContainer;
import dev.amble.lib.itemgroup.AItemGroup;
import net.minecraft.item.ItemStack;

public class PlushieItemGroups implements ItemGroupContainer {

    public static final AItemGroup PLUSHIES = AItemGroup.builder(AmbleKit.id("item_group"))
            .icon(() -> new ItemStack(PlushieBlocks.LOQOR_MARKETABLE_PLUSHIE)).build();
}