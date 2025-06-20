package net.minecraft.item;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Internal
public class InternalItemGroup0 extends ItemGroup {
    public InternalItemGroup0(Row arg, int i, Type arg2, Text arg3, Supplier<ItemStack> supplier, EntryCollector arg4, Identifier backgroundLocation, boolean hasSearchBar, int searchBarWidth, Identifier tabsImage, int labelColor, int slotColor, List<Identifier> tabsBefore, List<Identifier> tabsAfter) {
        super(arg, i, arg2, arg3, supplier, arg4, backgroundLocation, hasSearchBar, searchBarWidth, tabsImage, labelColor, slotColor, tabsBefore, tabsAfter);
    }
}
