package dev.drtheo.yaar.state;

import dev.drtheo.yaar.event.TEvent;
import dev.drtheo.yaar.event.TEvents;
import org.jetbrains.annotations.Contract;

import java.util.function.Supplier;

/**
 * A {@link RuntimeException} that <i>usually</i> gets handled internally. Used to exit early on {@link TStateContainer#state(TState.Type)}.
 * @apiNote To handle this exception use events instead of running behaviors directly or use {@link TEvent}s' {@code #handleSilent} methods.
 *
 * @see TEvent#handleSilent(TEvent, TEvents, Runnable)
 * @see TEvent#handleSilent(TEvent, TEvents, Supplier, Object)
 * @see TEvent#handleSilent(TEvent, TEvents, Supplier, Supplier)
 * @author DrTheodor (DrTheo_)
 */
public class StateResolveError extends RuntimeException {

    private static final StateResolveError INSTANCE = new StateResolveError();

    private StateResolveError() {
        this.setStackTrace(new StackTraceElement[0]);
    }

    private StateResolveError(TStateContainer container, TState.Type<?> type) {
        super("Couldn't find property '" + type.id + "' in container " + container);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (TStateRegistry.debug) return super.fillInStackTrace();

        this.setStackTrace(new StackTraceElement[0]);
        return this;
    }

    /**
     * Creates a new {@link StateResolveError} instance if {@link TStateRegistry#debug} is {@code true},
     * otherwise, re-uses a cached instance with no trace information.
     *
     * @param container the holder of the state.
     * @param type the state's type.
     * @return a {@link StateResolveError} instance.
     */
    @Contract(pure = true)
    public static StateResolveError create(TStateContainer container, TState.Type<?> type) {
        if (TStateRegistry.debug)
            return new StateResolveError(container, type);

        return INSTANCE;
    }
}
