package dev.drtheo.yaar.state;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for all classes that do NBT deserialization.
 *
 * @param <T> the target of deserialization.
 * @author DrTheodor (DrTheo_)
 */
@FunctionalInterface
public interface NbtDeserializer<T> {

    /**
     * Serializes the object to NBT.
     *
     * @param nbt the {@link NbtCompound} to deserialize from.
     * @param isClient whether the deserialization is happening on a client.
     * @return the deserialized object's instance.
     */
    @Contract(pure = true)
    T fromNbt(@NotNull NbtCompound nbt, boolean isClient);
}
