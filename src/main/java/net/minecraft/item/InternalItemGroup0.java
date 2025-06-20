package net.minecraft.item;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public class InternalItemGroup0 extends ItemGroup {
    private static final Identifier CREATIVE_INVENTORY_TABS_IMAGE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private static final Identifier BACKGROUND = new Identifier("textures/gui/container/creative_inventory/tab_items.png");

    public InternalItemGroup0(Row row, int column, Type type, Text displayName, Supplier<ItemStack> icon, EntryCollector entryCollector) {
        super(row, column, type, displayName, icon, entryCollector, BACKGROUND, false, 0, CREATIVE_INVENTORY_TABS_IMAGE, 4210752, -2130706433, new ArrayList<>(), new ArrayList<>());
    }
}
