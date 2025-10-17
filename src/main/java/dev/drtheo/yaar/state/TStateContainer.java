package dev.drtheo.yaar.state;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that represents a {@link TState} container.
 * @author DrTheodor (DrTheo_)
 */
public interface TStateContainer {

    /**
     * Gets the queried {@link TState} by its {@link TState.Type} or {@code null}.
     *
     * @param type the {@link TState}'s type.
     * @return queried {@link TState} or {@code null}.
     * @param <T> the state.
     */
    @Contract(pure = true)
    <T extends TState<T>> @Nullable T stateOrNull(@NotNull TState.Type<T> type);

    /**
     * Utility method that gets the queried {@link TState}
     * by its {@link TState.Type} or exits early if it wasn't found.
     *
     * @param type the {@link TState}'s type.
     * @return the queried state's instance.
     * @param <T> the state.
     * @throws StateResolveError if the state is not found. Handled internally (most of the time).
     * @see StateResolveError
     */
    @Contract(pure = true)
    default <T extends TState<T>> @NotNull T state(@NotNull TState.Type<T> type) {
        T result = stateOrNull(type);

        if (result == null)
            throw StateResolveError.create(this, type);

        return result;
    }

    /**
     * Utility method that gets the queried {@link TState} by its {@link TState.Type},
     * or exits while throwing {@link IllegalStateException} if it wasn't found.
     * <br>
     * Use this to enforce state existence.
     *
     * @param type the {@link TState}'s type.
     * @return the queried state's instance.
     * @param <T> the state.
     * @throws IllegalStateException if the state is not found.
     */
    @Contract(pure = true)
    default <T extends TState<T>> T resolveState(@NotNull TState.Type<T> type) {
        T res = stateOrNull(type);

        if (res == null)
            throw new IllegalStateException("Expected " + type.id());

        return res;
    }

    /**
     * Removes the {@link TState} by its {@link TState.Type}.
     *
     * @param type the {@link TState}'s type.
     * @return the removed {@link TState}, or null.
     * @param <T> the state.
     */
    @Contract(mutates = "this")
    <T extends TState<T>> @Nullable T removeState(@NotNull TState.Type<T> type);

    /**
     * Adds the {@link TState}.
     * @implNote Fails if the state is already present.
     *
     * @param state the {@link TState} to add.
     * @return whether adding the state had succeeded.
     */
    @Contract(mutates = "this")
    boolean addState(@NotNull TState<?> state);

    /**
     * Checks for existence of a state by its {@link TState.Type}, returning a proper {@code boolean} value.
     * May be slow for some implementations of {@link TStateContainer}, but the default implementation
     * {@link TStateContainer.ArrayBacked} can do it in O(1)
     *
     * @param type the {@link TState}'s type.
     * @return whether the state exists.
     */
    @Contract(pure = true)
    default boolean hasState(@NotNull TState.Type<?> type) {
        return stateOrNull(type) != null;
    }

    /**
     * Iterates through all (usually, non-null) entries of this {@link TStateContainer}.
     *
     * @param consumer the iterator that will consume the entries.
     */
    @Contract(pure = true)
    void forEachState(@NotNull Iterator consumer);

    @FunctionalInterface
    interface Iterator {
        void consume(int index, @Nullable TState<?> state);
    }

    /**
     * A basic implementation of {@link TStateContainer} that's backed by an array.
     */
    class ArrayBacked implements TStateContainer {

        private static final Object REMOVED = new Object();
        private final Object[] data;

        /**
         * @param maxSize the maximum size of the array.
         */
        @Contract(pure = true)
        protected ArrayBacked(int maxSize) {
            data = new Object[maxSize];
        }

        @Override
        @Contract(pure = true)
        @SuppressWarnings("unchecked")
        public <T extends TState<T>> @Nullable T stateOrNull(@NotNull TState.Type<T> type) {
            int index = type.index;

            if (index < 0)
                return null;

            Object res = data[index];
            return res == REMOVED ? null : (T) res;
        }

        @Override
        @Contract(mutates = "this")
        @SuppressWarnings("unchecked")
        public <T extends TState<T>> @Nullable T removeState(@NotNull TState.Type<T> type) {
            T result = (T) data[type.index];
            data[type.index] = REMOVED;

            return result;
        }

        @Override
        @Contract(mutates = "this")
        public boolean addState(@NotNull TState<?> state) {
            int index = state.type().verifyIndex();
            data[index] = state;
            return true;
        }

        @Override
        @Contract(pure = true)
        public void forEachState(@NotNull Iterator consumer) {
            Object[] objects = this.data;
            for (int i = 0; i < objects.length; i++) {
                Object state = objects[i];

                if (state == null)
                    continue;

                if (state == REMOVED) {
                    state = null;
                    objects[i] = null;
                }

                consumer.consume(i, (TState<?>) state);
            }
        }
    }

    /**
     * A delegate implementation of {@link TStateContainer} that delegates all the work to a parent {@link TStateContainer}.
     * @apiNote extend this class for usage.
     */
    abstract class Delegate implements TStateContainer {

        private final TStateContainer parent;

        /**
         * @param container the parent container to delegate {@link TStateContainer} calls to.
         */
        @Contract(pure = true)
        public Delegate(TStateContainer container) {
            this.parent = container;
        }

        @Override
        @Contract(pure = true)
        public <T extends TState<T>> @Nullable T stateOrNull(@NotNull TState.Type<T> type) {
            return parent.stateOrNull(type);
        }

        @Override
        @Contract(pure = true)
        public <T extends TState<T>> T resolveState(@NotNull TState.Type<T> type) {
            return parent.resolveState(type);
        }

        @Override
        public <T extends TState<T>> @NotNull T state(@NotNull TState.Type<T> type) {
            return parent.state(type);
        }

        @Override
        @Contract(mutates = "this")
        public <T extends TState<T>> @Nullable T removeState(@NotNull TState.Type<T> type) {
            return parent.removeState(type);
        }

        @Override
        @Contract(mutates = "this")
        public boolean addState(@NotNull TState<?> state) {
            return parent.addState(state);
        }

        @Override
        @Contract(pure = true)
        public void forEachState(@NotNull Iterator consumer) {
            parent.forEachState(consumer);
        }
    }
}
