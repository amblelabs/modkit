package dev.drtheo.yaar.state;

import dev.drtheo.yaar.event.TEventsRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base interface for all states.
 *
 * @param <Self> itself.
 * @author DrTheodor (DrTheo_)
 */
public interface TState<Self extends TState<Self>> {

    /**
     * @return the type of the state.
     */
    @Contract(pure = true)
    Type<Self> type();

    /**
     * A {@link SerializableType} backed by NBT.
     * @implNote All implementations must implement {@link NbtSerializer} for their {@link TState}.
     *
     * @param <T> the state.
     */
    abstract class NbtBacked<T extends TState<T> & NbtSerializer> extends SerializableType<T, NbtCompound> implements NbtDeserializer<T> {

        @Contract(pure = true)
        public NbtBacked(@NotNull Identifier id) {
            super(id);
        }

        @Override
        @Contract(pure = true)
        public @NotNull NbtCompound encode(@NotNull T t, boolean isClient) {
            NbtCompound nbt = new NbtCompound();
            t.toNbt(nbt, isClient);
            return nbt;
        }

        @Override
        @Contract(pure = true)
        public @NotNull T decode(@NotNull NbtCompound element, boolean isClient) {
            try {
                return this.fromNbt(element, isClient);
            } catch (Exception e) {
                TEventsRegistry.LOGGER.info(element.toString());
                throw e;
            }
        }
    }

    /**
     * A state backed by something serializable. All state that needs to be serialized (persisted and/or synced) must use
     * this state type.
     *
     * @param <T> the state.
     * @see NbtBacked
     */
    abstract class SerializableType<T extends TState<T>, S> extends Type<T> {

        /**
         * Constructs a new state type with the provided {@link Identifier}, which is later used for registration.
         *
         * @param id the state's {@link Identifier}.
         */
        @Contract(pure = true)
        public SerializableType(@NotNull Identifier id) {
            super(id);
        }

        /**
         * Decodes the object and creates a new instance.
         *
         * @param s serialized data.
         * @param isClient whether the deserialization is running on client.
         * @return a new {@link T} instance, containing all the deserialized data.
         */
        @Contract(pure = true)
        public abstract @NotNull T decode(@NotNull S s, boolean isClient);

        /**
         * Encodes the object.
         *
         * @param t the unserialized state.
         * @param isClient whether the serialization is running on client.
         * @return a new {@link S} instance, containing all the serialized data.
         */
        @Contract(pure = true)
        public abstract @NotNull S encode(@NotNull T t, boolean isClient);
    }

    /**
     * Base type for {@link TState}s. Used in registration.
     *
     * @param <T> the state.
     */
    @SuppressWarnings("unused")
    class Type<T extends TState<T>> {

        protected int index = -1;
        protected final @NotNull Identifier id;

        /**
         * Constructs a new state type with the provided {@link Identifier}, which is later used for registration.
         *
         * @param id the state's {@link Identifier}.
         */
        @Contract(pure = true)
        public Type(@NotNull Identifier id) {
            this.id = id;
        }

        /**
         * @return the state's {@link Identifier}.
         */
        @Contract(pure = true)
        public @NotNull Identifier id() {
            return id;
        }

        /**
         * Verifies the registration index and throws an exception in case of failure.
         * Used for quick access of {@link TState}s by {@link TStateContainer}s.
         *
         * @return the {@link Type}'s index.
         * @throws IllegalStateException if the {@link Type} is not registered.
         */
        @Contract(pure = true)
        public int verifyIndex() {
            if (index < 0)
                throw new IllegalStateException();

            return index;
        }

        @Override
        public String toString() {
            return "Type{" +
                    "index=" + index +
                    ", id=" + id +
                    '}';
        }

        @Override
        public int hashCode() {
            return index;
        }
    }
}
