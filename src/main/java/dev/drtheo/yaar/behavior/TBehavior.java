package dev.drtheo.yaar.behavior;

import dev.drtheo.yaar.event.TEvent;
import dev.drtheo.yaar.event.TEvents;
import org.jetbrains.annotations.Contract;

/**
 * Base interface for all behaviors (classes that handle logic).
 * All behaviors shouldn't be instantiated more than once.
 * <br>
 * <br>
 * If you want to obtain a reference to another {@link TBehavior}, either use the singleton pattern (not recommended),
 * or use DI with {@link Resolve} and {@link #behavior()}.
 * @author DrTheodor (DrTheo_)
 */
public interface TBehavior {

    /**
     * @implNote Never changes value, just a marker/stub value for final fields.
     * @return Always {@code null}.
     */
    @Contract(pure = true)
    default <T> T behavior() {
        return null;
    }

    /**
     * Redirects to {@link TEvents#handle(TEvent)}.
     */
    default <T extends TEvents> void handle(TEvent<T> event) {
        TEvents.handle(event);
    }

    /**
     * Redirects to {@link TEvents#handle(TEvent.Result)}.
     */
    default <T extends TEvents, R> R handle(TEvent.Result<T, R> event) {
        return TEvents.handle(event);
    }
}
