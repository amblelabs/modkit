package dev.drtheo.yaar.event;

import dev.drtheo.yaar.state.StateResolveError;
import org.jetbrains.annotations.Contract;

import java.util.function.Supplier;

/**
 * Base interface for all events.
 *
 * @param <T> the event group implementing {@link TEvents}.
 * @author DrTheodor (DrTheo_)
 */
public interface TEvent<T extends TEvents> {

    /**
     * The type of the event group. Should be the same as the associated {@link TEvents}' ({@link T}).
     *
     * @return the type of the event group.
     */
    @Contract(pure = true)
    TEvents.BaseType<T> type();

    /**
     * A method used to execute all the subscribed behaviors.
     * This allows for custom events to handle events differently (e.g. do early exit).
     *
     * @param subscribed an iterable of subscribed event handlers, implementing {@link T}.
     */
    void handleAll(Iterable<T> subscribed);

    /**
     * A helper method that runs some arbitrary code for a specific handler and event,
     * catching built-in non-fatal errors like {@link StateResolveError}.
     *
     * @param event the event that's currently being processed.
     * @param handler the event handler that handles the event.
     * @param r the runnable to execute.
     */
    static void handleSilent(TEvent<?> event, TEvents handler, Runnable r) {
        try {
            r.run();
        } catch (StateResolveError ignored) {
            // ignored
        } catch (Throwable e) {
            TEventsRegistry.LOGGER.error("Failed to handle event '{}' for handler '{}'", event.getClass(), handler.getClass(), e);
        }
    }

    /**
     * A helper method that runs some arbitrary code for a specific handler and event,
     * catching built-in non-fatal errors like {@link StateResolveError} and returns a value.
     *
     * @param <R> the return type.
     * @param event the event that's currently being processed.
     * @param handler the event handler that handles the event.
     * @param s the supplier to execute.
     * @param def the default value, in case of a fail.
     * @return the result from the supplier, or, the default value, if failed.
     */
    static <R> R handleSilent(TEvent<?> event, TEvents handler, Supplier<R> s, Supplier<R> def) {
        try {
            return s.get();
        } catch (StateResolveError ignored) {
            // ignored
        } catch (Throwable e) {
            TEventsRegistry.LOGGER.error("Failed to handle event '{}' for handler '{}'", event.getClass(), handler.getClass(), e);
        }

        return def.get();
    }

    /**
     * A helper method that runs some arbitrary code for a specific handler and event,
     * catching built-in non-fatal errors like {@link StateResolveError} and returns a value.
     *
     * @param event the event that's currently being processed.
     * @param handler the event handler that handles the event.
     * @param s the supplier to execute.
     * @param def the default value, in case of a fail.
     * @return the result from the supplier, or, the default value, if failed.
     */
    static <R> R handleSilent(TEvent<?> event, TEvents handler, Supplier<R> s, R def) {
        return handleSilent(event, handler, s, (Supplier<R>) () -> def);
    }

    /**
     * A built-in event implementation that just executes all the handlers without any result.
     *
     * @param <T> the event group.
     * @author DrTheodor (DrTheo_)
     */
    interface Notify<T extends TEvents> extends TEvent<T> {
        void handle(T handler) throws StateResolveError;

        @Override
        default void handleAll(Iterable<T> subscribed) {
            for (T handler : subscribed) {
                handleSilent(this, handler, () -> this.handle(handler));
            }
        }
    }

    /**
     * A built-in event implementation that has a result. {@link #handleAll(Iterable)} should be implemented by the user.
     *
     * @param <T> the event group.
     * @param <R> the result type.
     *
     * @see #handleAll(Iterable)
     * @author DrTheodor (DrTheo_)
     */
    interface Result<T extends TEvents, R> extends TEvent<T> {
        R result();

        @Override
        void handleAll(Iterable<T> subscribed);
    }
}
