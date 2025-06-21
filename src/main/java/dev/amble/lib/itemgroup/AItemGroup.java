package dev.amble.lib.itemgroup;

import java.util.function.Supplier;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AItemGroup extends ItemGroup {

    private final Identifier id;

    protected AItemGroup(Identifier id, ItemGroup.Builder builder) {
        super(builder);

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

            ItemGroup.Builder builder = new ItemGroup.Builder(null, -1);

            if (this.special)
                builder = builder.special();

            if (!this.renderName)
                builder = builder.noRenderedName();

            if (!this.scrollbar)
                builder = builder.noScrollbar();

            builder = builder.texture(this.texture)
                    .displayName(this.displayName)
                    .icon(this.iconSupplier)
                    .entries(this.entryCollector)
                    .withTabFactory(b -> new AItemGroup(id, b));

            return (AItemGroup) builder.build();
        }
    }
}
