package dev.amble.lib.boat;

import dev.amble.lib.container.RegistryContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public abstract class BoatTypeContainer implements RegistryContainer<BoatTypeContainer.Holder> {
	@Override
	public @Nullable Registry<Holder> getRegistry() {
		return null;
	}

	protected static ABoatType register(Item item, Item chest, Block block) {
        return new Holder(new Pending(item, chest, block));
    }

    protected static ABoatType registerNormal(Item item, Block block) {
        return register(item, null, block);
    }

    protected static ABoatType registerChest(Item item, Block block) {
        return register(null, item, block);
    }

    record Pending(Item item, Item chest, Block block) implements ABoatType {

        @Override
        public BoatEntity.Type get() {
            throw new IllegalStateException("This boat type was not registered yet!");
        }

        public ABoatType register(Identifier id) {
            return BoatTypeRegistry.register(id, this.item, this.chest, this.block);
        }
    }

    public static final class Holder implements ABoatType {

        ABoatType child;

        Holder(ABoatType child) {
            this.child = child;
        }

        @Override
        public BoatEntity.Type get() {
            return child.get();
        }
    }

    public Class<Holder> getTargetClass() {
        return Holder.class;
    }

    @Override
    public void postProcessField(Identifier identifier, Holder value, Field field) {
        // Promotion
        value.child = ((Pending) value.child).register(identifier);
    }

    @Override
    public void finish() {
        BoatTypeRegistry.apply();
    }
}