package dev.drtheo.yaar.event.impl;

import dev.drtheo.yaar.event.TEvent;
import dev.drtheo.yaar.event.TEvents;
import dev.drtheo.yaar.state.StateResolveError;

public interface CancellableTEvent<T extends TEvents> extends TEvent<T> {

    boolean handle(T handler) throws StateResolveError;

    @Override
    default void handleAll(Iterable<T> subscribed) {
        for (T handler : subscribed) {
            boolean cancel = !TEvent.handleSilent(this, handler, () -> this.handle(handler), false);

            if (cancel)
                break;
        }
    }
}
