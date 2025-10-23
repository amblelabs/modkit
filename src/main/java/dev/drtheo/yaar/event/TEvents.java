package dev.drtheo.yaar.event;

import dev.drtheo.yaar.behavior.TBehavior;
import org.jetbrains.annotations.Contract;

import java.util.*;

/**
 * Base interface for all event groups.
 * @author DrTheodor (DrTheo_)
 */
public interface TEvents {

    /**
     * Handles an event without any return type.
     *
     * @param event the event to handle.
     * @param <T> the event group.
     */
    static <T extends TEvents> void handle(TEvent<T> event) {
        event.type().handle(event);
    }

    /**
     * Handles a {@link TEvent.Result} event.
     *
     * @param event the event to handle.
     * @return the result value from the event.
     * @param <T> the event group.
     * @param <R> the event result type.
     */
    static <T extends TEvents, R> R handle(TEvent.Result<T, R> event) {
        event.type().handle(event);
        return event.result();
    }

    /**
     * Base interface for all event group types.
     *
     * @param <T> the event group.
     * @author DrTheodor (DrTheo_)
     */
    interface BaseType<T extends TEvents> {
        /**
         * Handles whether the {@link TBehavior} is applicable for this event type, in which case the behavior gets subscribed to the event.
         *
         * @param behavior the behavior to check.
         * @return whether the behavior is applicable for this event type.
         */
        @Contract(pure = true)
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean isApplicable(TBehavior behavior);

        /**
         * Subscribes the {@link TBehavior} to the event.
         *
         * @param behavior the behavior to subscrive.
         */
        @Contract(pure = true)
        void subscribe(TBehavior behavior);

        /**
         * Handles the provided event instance.
         *
         * @param event the event to handle.
         */
        void handle(TEvent<T> event);
    }

    /**
     * A basic implementation of the {@link BaseType} interface for easier and more convenient usage.
     *
     * @param clazz the class of the event group.
     * @param handlers a list of subscribed event handlers.
     * @param <T> the event group.
     * @author DrTheodor (DrTheo_)
     */
    record Type<T extends TEvents>(Class<T> clazz, Deque<T> handlers) implements BaseType<T> {

        /**
         * A helper constructor for the object, initiates with an empty {@link ArrayList} for the handlers.
         *
         * @param clazz the class of the event group.
         */
        public Type(Class<T> clazz) {
            this(clazz, new ArrayDeque<>());
        }

        @Override
        @Contract(pure = true)
        @SuppressWarnings("unchecked")
        public void subscribe(TBehavior behavior) {
            if (!this.isApplicable(behavior))
                throw new IllegalArgumentException("you're crazy");

            handlers.add((T) behavior);
        }

        @Override
        public void handle(TEvent<T> event) {
            event.handleAll(handlers);
        }

        @Override
        @Contract(pure = true)
        public boolean isApplicable(TBehavior behavior) {
            return clazz.isInstance(behavior);
        }
    }
}
