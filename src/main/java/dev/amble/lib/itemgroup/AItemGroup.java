package dev.amble.lib.itemgroup;

import java.util.function.Supplier;

import dev.amble.lib.mixin.ItemGroupAccessors;
import net.minecraft.item.InternalItemGroup0;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AItemGroup extends InternalItemGroup0 {

    private final Identifier id;

    protected AItemGroup(Identifier id, Row row, int column, Type type, Text displayName, Supplier<ItemStack> iconSupplier, EntryCollector entryCollector) {
        super(row, column, type, displayName, iconSupplier, entryCollector);

        this.id = id;
    }

    public Identifier id() {
        return id;
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public static class Builder {

        private static final EntryCollector EMPTY_ENTRIES = (displayContext, entries) -> {};
        private Text displayName = null;
        private Supplier<ItemStack> iconSupplier = () -> ItemStack.EMPTY;

        private EntryCollector entryCollector = EMPTY_ENTRIES;
        private boolean scrollbar = true;
        private boolean renderName = true;
        private boolean special = false;
        private Type type = Type.CATEGORY;
        private String texture = "items.png";

        private final Identifier id;

        public Builder(Identifier id) {
            this.id = id;
        }

        public Builder displayName(Text displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder icon(Supplier<ItemStack> iconSupplier) {
            this.iconSupplier = iconSupplier;
            return this;
        }

        public Builder entries(EntryCollector entryCollector) {
            this.entryCollector = entryCollector;
            return this;
        }

        public Builder special() {
            this.special = true;
            return this;
        }

        public Builder noRenderedName() {
            this.renderName = false;
            return this;
        }

        public Builder noScrollbar() {
            this.scrollbar = false;
            return this;
        }

        protected Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder texture(String texture) {
            this.texture = texture;
            return this;
        }

        public AItemGroup build() {
            if ((this.type == Type.HOTBAR || this.type == Type.INVENTORY) && this.entryCollector != EMPTY_ENTRIES) {
                throw new IllegalStateException("Special tabs can't have display items");
            }

            if (this.displayName == null)
                this.displayName = Text.translatable("itemGroup." + id.getNamespace() + "." + id.getPath());

            AItemGroup itemGroup = new AItemGroup(this.id, null, -1, this.type, this.displayName, this.iconSupplier, this.entryCollector);

            ((ItemGroupAccessors) itemGroup).setSpecial(this.special);
            ((ItemGroupAccessors) itemGroup).setRenderName(this.renderName);
            ((ItemGroupAccessors) itemGroup).setScrollbar(this.scrollbar);
            ((ItemGroupAccessors) itemGroup).setTexture(this.texture);
            return itemGroup;
        }
    }
}
