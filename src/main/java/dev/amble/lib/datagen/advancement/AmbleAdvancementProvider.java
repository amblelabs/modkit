package dev.amble.lib.datagen.advancement;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AmbleAdvancementProvider extends FabricAdvancementProvider {

    private final List<Builder> builders = new ArrayList<>();

    public AmbleAdvancementProvider(FabricDataOutput output) {
        super(output);
    }

    public Builder create(Advancement parent, String name) {
        Builder result = new Builder(parent, name);
        builders.add(result);
        return result;
    }

    public Builder create(String name) {
        return create(null, name);
    }

    @Override
    public void generateAdvancement(Consumer<Advancement> consumer) {
        for (Builder builder : builders) {
            consumer.accept(builder.build());
        }
    }

    public class Builder {

        private final Advancement.Builder builder;

        private ItemConvertible item = Items.BARRIER;
        private boolean hidden = false;
        private AdvancementFrame frame = AdvancementFrame.TASK;
        private boolean announce = true;
        private boolean showToast = true;

        private final String name;

        public Builder(Advancement parent, String name) {
            this.builder = Advancement.Builder.create().parent(parent);
            this.name = name;
        }

        public Builder condition(String name, CriterionConditions conditions) {
            this.builder.criterion(name, conditions);
            return this;
        }

        public Builder icon(ItemConvertible item) {
            this.item = item;
            return this;
        }

        public Builder hidden() {
            this.hidden = true;
            return this;
        }

        public Builder frame(AdvancementFrame frame) {
            this.frame = frame;
            return this;
        }

        public Builder silent() {
            this.announce = false;
            return this;
        }

        public Builder noToast() {
            this.showToast = false;
            return this;
        }

        public Advancement build() {
            String modId = AmbleAdvancementProvider.this.output.getModId();

            return builder
                    .display(item,
                            Text.translatable("achievement." + modId + ".title." + name),
                            Text.translatable("achievement." + modId + ".description." + name),
                            null, frame, showToast, announce, hidden)
                    .build(advancement -> {}, modId + ":" + name);
        }
    }
}
