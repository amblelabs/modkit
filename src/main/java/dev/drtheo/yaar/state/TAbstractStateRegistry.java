package dev.drtheo.yaar.state;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for all {@link TState}s.
 * @author DrTheodor (DrTheo_)
 */
public abstract class TAbstractStateRegistry {

    private boolean frozen;

    /**
     * A publicly accessible flag used for {@link TState} debugging.
     * @apiNote currently only used for {@link StateResolveError} handling.
     */
    public static boolean debug;

    private final List<TState.Type<?>> comps = new ArrayList<>();
    private final Map<Identifier, TState.Type<?>> idToHolder = new HashMap<>();

    /**
     * Creates a new {@link TStateContainer.ArrayBacked} state holder if the registry is frozen.
     *
     * @return new {@link TStateContainer.ArrayBacked} state holder.
     * @throws IllegalStateException if the registry was not frozen yet.
     */
    @Contract(pure = true)
    public TStateContainer createArrayHolder() {
        if (!frozen)
            throw new IllegalStateException("Registry not frozen yet!");

        // TODO: add Archetypes to pass smaller max array size.
        return new TStateContainer.ArrayBacked(comps.size());
    }

    /**
     * Registers a new {@link TState} by its {@link TState.Type}.
     *
     * @param type the {@link TState}'s type.
     * @throws IllegalStateException if the registry is already frozen.
     */
    public void register(TState.Type<?> type) {
        if (frozen)
            throw new IllegalStateException("Registry already frozen");

        type.index = comps.size();

        comps.add(type);
        idToHolder.put(type.id(), type);
    }

    /**
     * Gets a registered {@link TState.Type} by its index, or null if it wasn't found.
     *
     * @param index the registered state type's index.
     * @return the found {@link TState}'s type or null.
     *
     * @implNote the index may very each run, so make sure to not use constants.
     */
    @Contract(pure = true)
    public TState.Type<?> get(int index) {
        return comps.get(index);
    }

    /**
     * Gets a registered {@link TState.Type} by its {@link Identifier}, or null if it wasn't found.
     *
     * @param id the state's {@link Identifier}.
     * @return the found {@link TState}'s type or null.
     */
    @Contract(pure = true)
    public TState.Type<?> get(Identifier id) {
        return idToHolder.get(id);
    }

    /**
     * @return the amount of registered {@link TState.Type}s.
     */
    @Contract(pure = true)
    public int size() {
        return comps.size();
    }

    /**
     * Freezes the registry.
     */
    @Contract(pure = true)
    public void freeze() {
        frozen = true;
    }
}
